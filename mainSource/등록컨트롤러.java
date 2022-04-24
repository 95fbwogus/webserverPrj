package boardController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import entity.Board;
import entity.ChildBoardInfo;
import entity.ParentBoardInfo;
import entity.ValidUser;
import filter.AdminCheckFilter;
import service.BoardService;
import service.LogService;
import service.ManageService;
import util.EncryptionUtil;
import util.FolderUtil;
import util.GetUserIp;
import util.IsValidLogin;
import util.IsValidValue;
import util.UploadCheck;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 50, maxRequestSize = 1024 * 1024 * 50 * 5)
@WebServlet("/board/reg")
public class BoardRegController extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		boolean filter = IsValidLogin.loginFilter(request); // 로그인 확인
		if (!filter) {
			response.sendRedirect("/login");
			return;
		}
		ValidUser who = (ValidUser) request.getSession().getAttribute("validUser");

		LogService lService = new LogService();
		ManageService mService = new ManageService();
		BoardService bService = new BoardService();

		String category_ =IsValidValue.preventXSS(request.getParameter("board"));
		int category = 0;
		
		if(category_ != null)
			category = Integer.parseInt(category_);
		else {
			response.sendError(404);
			return;
		}
		
		ChildBoardInfo cbi = bService.getChildBoardInfo(category);
		if(cbi == null) {
			response.sendError(404);
			return;
		}
		
		// 혹여나 관리자가 일반 계정을 사용하다가 관리자 계정으로 로그인 한 후, url 자동 완성으로 일반 사용자용 url이 입력되어 들어왔을
		// 경우.
		try {
			boolean adminFilter = IsValidLogin.adminFilter(request);
			if (adminFilter) {
				response.sendRedirect("/admin/board/reg?board=" + category);
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// 공지사항 게시판과 관리자용 영상 게시판은 일반 사용자가 글을 등록할 수 없음. 일반 사용자가 해당 카테고리를 들고 왔다면 스크립트 수정을
		// 통해 카테고리를 변경해서 넣었을 거임.
		if (cbi.getAdmin()) {
			try {
				boolean adminFilter = IsValidLogin.adminFilter(request);
				if (!adminFilter) {
					response.sendError(404);
					lService.insertUserLog(who.getId(), "비정상적인 관리자 권한 접근", GetUserIp.getClientIp(request),
							category + "번 게시판 게시글 등록");
					lService.insertFatalLog(who.getId(), who.getIp());
					mService.setBlockUser(who.getId(), who.getIp(), "서버 공격, 비정상 접근", 365);
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		List<ParentBoardInfo> listPbi = bService.getParentBoardInfo();
		String mainImg = mService.getMainImg();
		request.setAttribute("mainImg", mainImg);
		request.setAttribute("pb", listPbi); //parentBoard
		switch (cbi.getKind()) {
		case 1:
			request.getRequestDispatcher("/WEB-INF/view/board/boardReg/reg1.jsp").forward(request, response);
			break;
		case 2:
			request.getRequestDispatcher("/WEB-INF/view/board/boardReg/reg2.jsp").forward(request, response);
			break;
		case 3:
			request.getRequestDispatcher("/WEB-INF/view/board/boardReg/reg3.jsp").forward(request, response);
			break;
		case 4:
			request.getRequestDispatcher("/WEB-INF/view/board/boardReg/reg4.jsp").forward(request, response);
			break;
		default:
			lService.insertUserLog(who.getId(), "비정상 게시판 접근 시도", GetUserIp.getClientIp(request),
					category + "번 게시판 접근 시도");
			lService.insertFatalLog(who.getId(), who.getIp());
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		boolean filter = IsValidLogin.loginFilter(request); // 로그인 확인
		if (!filter) {
			response.sendRedirect("/login");
			return;
		}
		ValidUser who = (ValidUser) request.getSession().getAttribute("validUser");
		
		LogService lService = new LogService();
		BoardService bService = new BoardService();
		
		String category_ =IsValidValue.preventXSS(request.getParameter("board"));
		int category = 0;
		if(category_ != null)
			category = Integer.parseInt(category_);
		else {
			response.sendError(404);
			return;
		}
		
		ChildBoardInfo cbi = bService.getChildBoardInfo(category);
		if(cbi == null) {
			response.sendError(404);
			return;
		}
		
		if(cbi.getAdmin()) {
			response.sendError(404);
			lService.insertUserLog(who.getId(), "비정상적인 권한 접근", GetUserIp.getClientIp(request),
					category + "번 게시판 게시글 등록");
			lService.insertFatalLog(who.getId(), who.getIp());
		}

		StringBuilder files_ = new StringBuilder();
		PrintWriter out = response.getWriter();
		
		
		//여기부터 게시글 데이터
		String title = IsValidValue.preventXSS(request.getParameter("title"));
		String content = IsValidValue.preventXSS(request.getParameter("content"));
		if (content.length() > 5000) {
			content = content.substring(0, 4999);
		}
		if (title == null || content == null) {
			if (who == null) {
				lService.insertUserLog("비정상적인 사용자", "비정상적인 파일 업로드 시도", GetUserIp.getClientIp(request), "게시판 파일 업로드");
				lService.insertFatalLog("비정상적인 사용자", GetUserIp.getClientIp(request));
			} else {
				lService.insertUserLog(who.getId(), "비정상적인 파일 업로드 시도", GetUserIp.getClientIp(request), "게시판 파일 업로드");
				lService.insertFatalLog(who.getId(), GetUserIp.getClientIp(request));
			}
			out.println("-1");
			out.close();
			return;
		}

		files_.append(IsValidValue.preventXSS(request.getParameter("files")));
		String regdate_ = IsValidValue.preventXSS(request.getParameter("tmp").trim());
		long regdate = Long.parseLong(regdate_);

		// if (files_.length() != 0)
		// files_.delete(files_.length() - 1, files_.length());// 마지막 ',' 제거

		String tmpFileNames = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String hashId = EncryptionUtil.getFolderId(regdate);
		String fromPath = request.getServletContext().getRealPath("/upload/tmp/" + hashId);
		String toFolderName = format.format(regdate);
		String toPath = request.getServletContext()
				.getRealPath("/upload/board/" + cbi.getFolderName() + File.separator + toFolderName);

		File to = new File(toPath);// 오늘 가장 처음 만드는 폴더면 생성하고 파일 집어넣기.
		File from = new File(fromPath);// 임시 폴더

		if (!to.exists()) {
			try {
				to.mkdir(); // 폴더 생성합니다.
				tmpFileNames = FolderUtil.copy(from, to);
				FolderUtil.deleteFolder(fromPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			tmpFileNames = FolderUtil.copy(from, to); // 정상적으로 폴더에 저장한 파일들 반환
			FolderUtil.deleteFolder(fromPath);
		}

		Board board = new Board();
		board.setTitle(title);
		board.setContent(content);
		board.setNickname(who.getNickname());
		board.setWriterId(who.getId());
		board.setWriterIp(GetUserIp.getClientIp(request));
		board.setFiles(IsValidValue.rebuildingFiles(files_.toString(), tmpFileNames));
		board.setPub(true); // 일반 사용자는 무조건 바로 공개
		board.setUrg(false); // 일반 사용자는 무조건 false, 관리자만 설정 가능.
		board.setRegdate(format2.format(regdate));
		board.setCategory(category);

		lService.insertUserLog(who.getNickname(), category + "번 게시판 글 등록", GetUserIp.getClientIp(request),
				category + "번 게시판");

		int result = bService.insertBoard(board);
		out.println(result);
		out.close();
	}
}

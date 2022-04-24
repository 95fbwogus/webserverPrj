protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ValidUser who = (ValidUser) request.getSession().getAttribute("validUser");
		boolean filter = IsValidLogin.loginFilter(request);
		if (!filter) {
			response.sendRedirect("/login");
			return;
		}

		LogService lService = new LogService();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		PrintWriter out = response.getWriter();
		long millis = System.currentTimeMillis();
		String tmpFolder = EncryptionUtil.getFolderId(millis);
		String realPath = request.getServletContext().getRealPath("/upload/tmp/" + tmpFolder);
		Collection<Part> parts = request.getParts();
		// 폴더 생성
		for (int div = 0; div < 10; div++) { //정말 극악의 확률로 해쉬한 값에서 뽑아낸 것이 겹친 경우, 10번 정도는 재시도
			File Folder = new File(realPath);
			if (!Folder.exists()) {
				try {
					Folder.mkdir(); // 폴더 생성합니다.
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				millis = System.currentTimeMillis();
				tmpFolder = EncryptionUtil.getFolderId(millis);
				realPath = request.getServletContext().getRealPath("/upload/tmp/" + tmpFolder);
			}
		}
		//~폴더 생성 완료
		//위에서 생성한 폴더에 파일 넣기
		StringBuilder files_ = new StringBuilder();
		for (Part p : parts) {
			if (!p.getName().equals("files")) {
				continue; // 파일이 아니면 다시 parts에서 뽑아오기
			}
			if (p.getSize() == 0)
				continue; // 사이즈가 0이 아니면 패스

			Part filePart = p;
			String fileName = filePart.getSubmittedFileName();

			String path = realPath + File.separator;
			String filePath = null;

			// 파일명 유효성 검사 후 중복 확인
			if (UploadCheck.fileCheck(fileName)) {
				// 중복 확인은 게시글 등록 칸에서 진행
				filePath = path + fileName;
				files_.append(fileName);
				files_.append(",");
			} else {
				if (who == null) {
					lService.insertUserLog("비정상적인 사용자", "비정상적인 파일 업로드 시도", GetUserIp.getClientIp(request),
							"게시판 파일 업로드");
					lService.insertFatalLog("비정상적인 사용자", GetUserIp.getClientIp(request));
				} else {
					lService.insertUserLog(who.getId(), "비정상적인 파일 업로드 시도", GetUserIp.getClientIp(request),
							"게시판 파일 업로드");
					lService.insertFatalLog(who.getId(), GetUserIp.getClientIp(request));
				}
				out.println("-1");
				out.close();
				return;
			}
			InputStream fis = filePart.getInputStream();
			FileOutputStream fos = new FileOutputStream(filePath);

			byte[] buf = new byte[1024]; // 1KB
			int size = 0;

			while ((size = fis.read(buf)) != -1) {
				fos.write(buf, 0, size); // 마지막에 가져올 때는 딱 남은 만큼만 쓰도록 만들자. 읽어온 게 900개면 900바이트만 쓰도록.
			}

			fos.close();
			fis.close();
		}
		if (files_.length() != 0)
			files_.delete(files_.length() - 1, files_.length());// 마지막 ',' 제거

		String files = files_.toString();

		if (files.split(",").length > 10) {// 파일 개수 제한 넘음. 파일은 냅두고 일단 비정상 사용자로 등록
			lService.insertUserLog(who.getId(), "비정상적인 자바스크립트 수정", GetUserIp.getClientIp(request), "파일 업로드");
			lService.insertFatalLog(who.getId(), GetUserIp.getClientIp(request));
			out.println("0");
			return;
		}
		out.println(millis);
		out.close();
		return;
	}

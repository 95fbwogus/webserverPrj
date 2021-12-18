package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import entity.Board;
import entity.BoardView;
import entity.ChildBoardInfo;
import entity.CmtBody;
import entity.Comment;
import entity.ParentBoardInfo;
import entity.ReComment;
import util.DatabaseUtil;

@SuppressWarnings("finally")
public class BoardService {// �Խ��� �� ���, �����ڸ� urg�� ���� ��� ��ġ��Ű�ų�, pub = 0 ������ ����ó���� �� ����.
	public int insertCmt(Comment cmt) {
		int result = 0; // �⺻�� 0, ���������δ� �� ���� ���ԵǾ����� ����
		long lastNum = 0;
		String findLastNum = "select max(id) as id from board_comment where category = ? and board_id = ?;";
		String sql = "insert into board_comment(id, nickname, content, writer, ip, board_id, category) values(?,?,?,?,?,?,?);";
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement findPstmt = conn.prepareStatement(findLastNum);
			findPstmt.setInt(1, cmt.getCategory());
			findPstmt.setLong(2, cmt.getBoardId());
			ResultSet rs = findPstmt.executeQuery();

			if (rs.next())
				lastNum = rs.getLong("id");

			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setLong(1, lastNum + 1);
			pstmt.setString(2, cmt.getNickname());
			pstmt.setString(3, cmt.getContent());
			pstmt.setString(4, cmt.getWriterId());
			pstmt.setString(5, cmt.getWriterIp());
			pstmt.setLong(6, cmt.getBoardId());
			pstmt.setInt(7, cmt.getCategory());

			result = pstmt.executeUpdate();

			conn.commit();
			rs.close();
			findPstmt.close();
			pstmt.close();

		} catch (SQLException e) {
			if (conn != null)
				conn.rollback();
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return result; // ���� �߻� �� 0 ��ȯ
		}
	}

	public int insertReCmt(ReComment reCmt) {
		int result = 0; // �⺻�� 0, ���������δ� �� ���� ���ԵǾ����� ����
		long lastNum = 0;
		String findLastNum = "select max(id) as id from re_comment where category = ? and board_id = ? and cmt_id = ?;";
		String sql = "insert into re_comment(id, nickname, content, writer, ip, cmt_id, board_id, category) values(?,?,?,?,?,?,?,?);";
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement findPstmt = conn.prepareStatement(findLastNum);
			findPstmt.setInt(1, reCmt.getCategory());
			findPstmt.setLong(2, reCmt.getBoardId());
			findPstmt.setLong(3, reCmt.getCmtId());
			ResultSet rs = findPstmt.executeQuery();

			if (rs.next())
				lastNum = rs.getLong("id");

			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setLong(1, lastNum + 1);
			pstmt.setString(2, reCmt.getNickname());
			pstmt.setString(3, reCmt.getContent());
			pstmt.setString(4, reCmt.getWriterId());
			pstmt.setString(5, reCmt.getWriterIp());
			pstmt.setLong(6, reCmt.getCmtId());
			pstmt.setLong(7, reCmt.getBoardId());
			pstmt.setInt(8, reCmt.getCategory());

			result = pstmt.executeUpdate();

			conn.commit();
			rs.close();
			findPstmt.close();
			pstmt.close();

		} catch (SQLException e) {
			if (conn != null)
				conn.rollback();
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return result; // ���� �߻� �� 0 ��ȯ
		}
	}

	public int insertBoard(Board board) {
		int result = -1;
		long lastNum = 0;
		String findLastNum = "select max(id) as id from board where category = ?;";
		String sql = "insert into board(id, title, content, nickname, files, pub, urg, writer, ip, category, regdate) values(?,?,?,?,?,?,?,?,?,?,?);";
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement findPstmt = conn.prepareStatement(findLastNum);
			findPstmt.setInt(1, board.getCategory());
			ResultSet rs = findPstmt.executeQuery();

			if (rs.next())
				lastNum = rs.getLong("id");

			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setLong(1, ++lastNum);
			pstmt.setString(2, board.getTitle());
			pstmt.setString(3, board.getContent());
			pstmt.setString(4, board.getNickname());
			pstmt.setString(5, board.getFiles());
			pstmt.setBoolean(6, board.isPub());
			pstmt.setBoolean(7, board.isUrg());
			pstmt.setString(8, board.getWriterId());
			pstmt.setString(9, board.getWriterIp());
			pstmt.setInt(10, board.getCategory());
			pstmt.setString(11, board.getRegdate());

			pstmt.executeUpdate();

			conn.commit();
			rs.close();
			findPstmt.close();
			pstmt.close();

			// ���� �߻� ��, result�� lastNum ���� x
			result = (int) lastNum;
		} catch (SQLException e) {
			if (conn != null)
				conn.rollback();
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return result; // ���� �߻� �� 0�� ��ȯ��.
		}
	}

	// list�� �ѷ��� �Խ��� �Ϲ� �� ��������, �Ϲ� ����� pubCheck == false
	public List<BoardView> getBoardUrgList(boolean isAdmin, int category) {
		List<BoardView> list = new ArrayList<>();
		// sql�� ���� �� ���� �ϳ��ϳ� ��������.
		String normSql = "select id from(select *, ROW_NUMBER() over(order by regdate desc)"
				+ "as rnum from board where pub = true and urg = true and category = ?)N";
		String adminSql = "select id from(select *, ROW_NUMBER() over(order by regdate desc)"
				+ "as rnum from board where category = ? and urg = true)N;";

		String sql = (isAdmin ? adminSql : normSql);

		String sql2 = "select * from board_view where id = ? and category = ?";
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			PreparedStatement pstmt2 = null;
			ResultSet rs2 = null;
			pstmt.setInt(1, category);

			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				long id = rs.getLong("id");
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setLong(1, id);
				pstmt2.setInt(2, category);

				rs2 = pstmt2.executeQuery();
				rs2.next();
				String title = rs2.getString("title");
				String nickname = rs2.getString("nickname");
				String regdate = rs2.getString("regdate");
				int hit = rs2.getInt("hit");
				String files = rs2.getString("files");
				int cmtCount = rs2.getInt("cmt_count");
				boolean urg = rs2.getBoolean("urg");
				boolean pub = rs2.getBoolean("pub");
				BoardView board = new BoardView(id, title, nickname, regdate, hit, files, pub, cmtCount, urg);
				list.add(board);
			}

			rs.close();
			rs2.close();
			pstmt.close();
			pstmt2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return list;
		}
	}

	public List<BoardView> getBoardNormList(boolean isAdmin, int category) {
		return getBoardNormList("title", "", 1, isAdmin, category);
	}

	public List<BoardView> getBoardNormList(int page, boolean isAdmin, int category) {
		return getBoardNormList("title", "", page, isAdmin, category);
	}

	// list�� �ѷ��� �Խ��� ���� �� ��������, �Ϲ� ����� pubCheck == false
	public List<BoardView> getBoardNormList(String field, String query, int page, boolean isAdmin, int category) {
		List<BoardView> list = new ArrayList<>();
		// sql�� ���� �� ���� �ϳ��ϳ� ��������.
		String normSql = "select id from(select *, ROW_NUMBER() over(order by regdate desc)"
				+ "as rnum from board where " + field + " like ? and pub = true and urg = false and category = ?) "
				+ "N where rnum between ? and ?;";
		String adminSql = "select id from(select *, ROW_NUMBER() over(order by regdate desc)"
				+ "as rnum from board where " + field + " like ? and category = ? and urg = false) "
				+ "N where rnum between ? and ?;";

		String sql = (isAdmin ? adminSql : normSql);

		String sql2 = "select * from board_view where id = ? and category = ?";
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			PreparedStatement pstmt2 = null;
			ResultSet rs2 = null;
			pstmt.setString(1, "%" + query + "%");
			pstmt.setInt(2, category);
			pstmt.setInt(3, 1 + (page - 1) * 10); // ���޵� �������� ���� ��ȣ, �� �������� 10���� �������� ���´�.
			pstmt.setInt(4, page * 10);// �������� �� ��ȣ, 1~10 / 11~20 ...
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				long id = rs.getLong("id");
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setLong(1, id);
				pstmt2.setInt(2, category);

				rs2 = pstmt2.executeQuery();
				rs2.next();
				String title = rs2.getString("title");
				String nickname = rs2.getString("nickname");
				String regdate = rs2.getString("regdate");
				int hit = rs2.getInt("hit");
				String files = rs2.getString("files");
				int cmtCount = rs2.getInt("cmt_count");
				boolean urg = rs2.getBoolean("urg");
				boolean pub = rs2.getBoolean("pub");
				BoardView board = new BoardView(id, title, nickname, regdate, hit, files, pub, cmtCount, urg);
				list.add(board);
			}

			rs.close();
			rs2.close();
			pstmt.close();
			pstmt2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return (list.isEmpty()) ? null : list;
		}
	}

	// ��� �������� ī��Ʈ
	public long getCmtCount(long boardId, int category) {
		String sql = "select count(id) from board_comment where board_id = ? and category = ?;";
		long count = 0;
		Connection conn = DatabaseUtil.getConnection();

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, boardId);
			pstmt.setInt(2, category);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				count = rs.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return count;
		}
	}

	// �˻��� ����, ó�� �������� ��� �������� ������ ��,
	public int getBoardCount(boolean isAdmin, int category) {
		return getBoardCount("title", "", isAdmin, category);
	}

	public int getBoardCount(String field, String query, boolean isAdmin, int category) {
		int count = 0; // �ش� �˻� �з�/�˻���� ã�� ���ϸ� 0�� ��ȯ
		String normSql = "select count(*) from (select * from(select *, ROW_NUMBER() "
				+ "over(order by regdate desc) as rnum from board where " + field
				+ " like ? and pub = true and urg = false and category = ?)N)N1;";

		String adminSql = "select count(*) from (select * from(select *, ROW_NUMBER() "
				+ "over(order by regdate desc) as rnum from board where " + field
				+ " like ? and category = ? and urg = false)N)N1;";

		String sql = (isAdmin ? adminSql : normSql);

		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + query + "%");
			pstmt.setInt(2, category);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				count = rs.getInt(1);

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return count;
		}
	}

	public CmtBody getCmt(CmtBody cmt, boolean isAdmin) {
		String getCmt = "select writer from board_comment where id = ? and board_id = ? and category = ?;";
		String getRcmt = "select writer from re_comment where id = ? and cmt_id = ? and board_id = ? and category = ?;";
		CmtBody tmp = null;
		PreparedStatement pstmt = null;
		Connection conn = DatabaseUtil.getConnection();
		ResultSet rs = null;
		try {
			if (cmt instanceof Comment) {
				pstmt = conn.prepareStatement(getCmt);
				pstmt.setLong(1, ((Comment) cmt).getId());
				pstmt.setLong(2, ((Comment) cmt).getBoardId());
				pstmt.setInt(3, ((Comment) cmt).getCategory());

				rs = pstmt.executeQuery();
				if (rs.next()) {
					Comment cmtPlate = new Comment();
					cmtPlate.setWriterId(rs.getString("writer"));
					tmp = cmtPlate;
				}
			} else {
				pstmt = conn.prepareStatement(getRcmt);
				pstmt.setLong(1, ((ReComment) cmt).getId());
				pstmt.setLong(2, ((ReComment) cmt).getCmtId());
				pstmt.setLong(3, ((ReComment) cmt).getBoardId());
				pstmt.setInt(4, ((ReComment) cmt).getCategory());

				rs = pstmt.executeQuery();
				if (rs.next()) {
					ReComment rCmtPlate = new ReComment();
					rCmtPlate.setWriterId(rs.getString("writer"));
					tmp = rCmtPlate;
				}
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				conn.rollback();
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return tmp;
		}
	}

	public Board getBoard(long id, int category, boolean isAdmin) {
		Board board = null;
		String normSql = "select * from board where id = ? and pub = true and category = ?;";
		String adminSql = "select * from board where id = ? and category = ?;";

		String sql = (isAdmin ? adminSql : normSql);

		String hitUpdate = "update board set hit = ? where id = ? and category = ?;";
		int hit = 0;
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, id);
			pstmt.setInt(2, category);

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				String title = rs.getString("title");
				String content = rs.getString("content");
				String nickname = rs.getString("nickname");
				String ip = rs.getString("ip");
				String regdate = rs.getString("regdate");
				String files = rs.getString("files");
				String writerId = rs.getString("writer");
				boolean pub = rs.getBoolean("pub");
				boolean urg = rs.getBoolean("urg");
				hit = rs.getInt("hit");
				hit++;

				// ��ȸ�� �۾�-------------------
				conn.setAutoCommit(false);
				PreparedStatement updatePstmt = conn.prepareCall(hitUpdate);
				updatePstmt.setInt(1, hit);
				updatePstmt.setLong(2, id);
				updatePstmt.setInt(3, category);
				updatePstmt.executeUpdate();
				conn.commit();
				updatePstmt.close();
				// -----------------------------

				board = new Board(id, title, nickname, writerId, ip, regdate, hit, files, content, pub, urg);
				board.setCategory(category);
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				conn.rollback();
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return board;
		}
	}

	// �Ϲ� ����ڳ� admin�� detail���� ���� �۷� �Ѿ �� �ְ� id�� title�� �Ѱ��ش�. �Ϲ� ����� pubCheck ==
	// true, urg = false�� ������ �Ű��� �������� �ƴ� �Ϲݱ��� �������� ����.. urg = true�� �Խ��� ��ܿ� �����ų
	// �������� �������� ����.
	public Board getNextBoard(long id, int category, boolean isAdmin, boolean urg) {
		// String sql = "select id, title, pub from board where id = (select id from
		// (select id, @rownum:=@rownum+1 as rnum from board, (select @rownum:=0) as R
		// where regdate >(select regdate from board where id=? and category = ? and pub
		// = true))N where
		// rnum =1) and category = ?;";
		String normSql = "select id, title from board where id = (select min(id) as id from board where regdate > (select regdate from board where id = ? and category = ?) and category = ? and pub = true and urg = ?) and category = ?;";
		String adminSql = "select id, title, pub from board where id = (select min(id) as id from board where regdate > (select regdate from board where id = ? and category = ?) and category = ? and urg = ?) and category = ?;";

		String sql = (isAdmin ? adminSql : normSql);

		Board board = null;

		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, id);
			pstmt.setInt(2, category);
			pstmt.setInt(3, category);
			pstmt.setBoolean(4, urg);
			pstmt.setInt(5, category);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				long nextId = rs.getLong("id");
				String title = rs.getString("title");
				boolean pub = (isAdmin ? rs.getBoolean("pub") : true);

				board = new Board(nextId, title, pub);
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return board;
		}
	}

	public Board getPrevBoard(long id, int category, boolean isAdmin, boolean urg) {
		String normSql = "select id, title from board where id = (select max(id) as id from board where regdate < (select regdate from board where id = ? and category = ?) and category = ? and pub = true and urg = ?) and category = ?;";
		String adminSql = "select id, title, pub from board where id = (select max(id) as id from board where regdate < (select regdate from board where id = ? and category = ?) and category = ? and urg = ?) and category = ?;";
		String sql = (isAdmin ? adminSql : normSql);
		Board board = null;

		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, id);
			pstmt.setInt(2, category);
			pstmt.setInt(3, category);
			pstmt.setBoolean(4, urg);
			pstmt.setInt(5, category);

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				long prevId = rs.getLong("id");
				String title = rs.getString("title");
				boolean pub = (isAdmin ? rs.getBoolean("pub") : true);
				board = new Board(prevId, title, pub);
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return board;
		}
	}

	// 1���� �Խñ� ����, ����� ����
	public int updatePubBoard(Board board) {
		int result = 0; // ������Ʈ �Ϸ�� Ʃ�� ���� ����
		String sql = "update board set pub = ? where id = ? and category = ?;";
		Connection conn = DatabaseUtil.getConnection();

		try {// ���� ����� -1�� ���Ƿ� ���� ���� ó�� �� ���൵ ��.
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setBoolean(1, board.isPub());
			pstmt.setLong(2, board.getId());
			pstmt.setInt(3, board.getCategory());
			result = pstmt.executeUpdate();
			conn.commit();
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; // ������Ʈ�� �� Ʃ�� ���� ��ȯ.
		}
	}

	// �Խñ� ����, ����� ����
	public int updatePubInBoard(List<String> oids, List<String> cids, int category) {
		String oidsJoin = String.join(",", oids); // ����Ʈ�� ��Ƴ��� id���� DB�� ������ �� �ֵ��� 1,2,3... ���� ��������
		String cidsJoin = String.join(",", cids);
		int result = 0; // ������Ʈ �Ϸ�� Ʃ�� ���� ����

		String updateOpen = "update board set pub = true where id in(" + oidsJoin + ") and category = ?";
		String updateClose = "update board set pub = false where id in(" + cidsJoin + ") and category = ?;";
		Connection conn = DatabaseUtil.getConnection();

		try {// ���� ����� -1�� ���Ƿ� ���� ���� ó�� �� ���൵ ��.
			conn.setAutoCommit(false);
			PreparedStatement pstmtOpen = conn.prepareStatement(updateOpen);
			pstmtOpen.setInt(1, category);
			result += pstmtOpen.executeUpdate();
			if (!cidsJoin.equals("")) { // ����� ����� �ƹ��͵� ������ []�� ���޵ǹǷ� DB ������ ���� �߻�
				PreparedStatement pstmtClose = conn.prepareStatement(updateClose);
				pstmtClose.setInt(1, category);
				result += pstmtClose.executeUpdate();
				pstmtClose.close();
			}
			conn.commit();
			pstmtOpen.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; // ������Ʈ�� �� Ʃ�� ���� ��ȯ.
		}
	}

	// 1���� �Խñ� ����, ����� ����
	public int updateBoardFiles(Board board) {
		int result = 0; // ������Ʈ �Ϸ�� Ʃ�� ���� ����
		String sql = "update board set files = ? where id = ? and category = ?;";
		Connection conn = DatabaseUtil.getConnection();

		try {// ���� ����� -1�� ���Ƿ� ���� ���� ó�� �� ���൵ ��.
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, board.getFiles());
			pstmt.setLong(2, board.getId());
			pstmt.setInt(3, board.getCategory());
			result = pstmt.executeUpdate();
			conn.commit();
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; // ������Ʈ�� �� Ʃ�� ���� ��ȯ.
		}
	}

	// �ϳ��� ���,���� ���� (�ַ� �Ϲ� ����ڵ�, �Ű� �Խ��ǿ����� �̿�)
	public int delCmt(CmtBody cmt) {
		int result = 0;
		String delCmt = "update board_comment set del = 1, ip = ? where id = ? and board_id =? and category = ?";
		String delReCmt = "update re_comment set del = 1, ip = ? where id = ? and cmt_id = ? and board_id =? and category = ?";
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = null;
			if (cmt instanceof Comment) {
				conn.setAutoCommit(false);
				pstmt = conn.prepareStatement(delCmt);
				pstmt.setString(1, ((Comment) cmt).getWriterIp());
				pstmt.setLong(2, ((Comment) cmt).getId());
				pstmt.setLong(3, ((Comment) cmt).getBoardId());
				pstmt.setInt(4, ((Comment) cmt).getCategory());
				result = pstmt.executeUpdate();
				conn.commit();
			} else if (cmt instanceof ReComment) {
				conn.setAutoCommit(false);
				pstmt = conn.prepareStatement(delReCmt);
				pstmt.setString(1, ((ReComment) cmt).getWriterIp());
				pstmt.setLong(2, ((ReComment) cmt).getId());
				pstmt.setLong(3, ((ReComment) cmt).getCmtId());
				pstmt.setLong(4, ((ReComment) cmt).getBoardId());
				pstmt.setInt(5, ((ReComment) cmt).getCategory());
				result = pstmt.executeUpdate();
				conn.commit();
			}
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
	}

	// �ϳ��� ��� ���� (�����ڿ�)
	public int perDelCmt(CmtBody cmt) {
		int result = 0;
		String perDelCmt = "delete from board_comment where id = ? and board_id = ? and category = ? and del = true";
		String perDelReCmt = "delete from re_comment where id = ? and cmt_id = ? and board_id = ? and category = ? and del = true";
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = null;
			if (cmt instanceof Comment) {
				conn.setAutoCommit(false);
				pstmt = conn.prepareStatement(perDelCmt);
				pstmt.setLong(1, ((Comment) cmt).getId());
				pstmt.setLong(2, ((Comment) cmt).getBoardId());
				pstmt.setInt(3, ((Comment) cmt).getCategory());
				result = pstmt.executeUpdate();
				conn.commit();
			} else if (cmt instanceof ReComment) {
				conn.setAutoCommit(false);
				pstmt = conn.prepareStatement(perDelReCmt);
				pstmt.setLong(1, ((ReComment) cmt).getId());
				pstmt.setLong(2, ((ReComment) cmt).getCmtId());
				pstmt.setLong(3, ((ReComment) cmt).getBoardId());
				pstmt.setInt(4, ((ReComment) cmt).getCategory());
				result = pstmt.executeUpdate();
				conn.commit();
			}
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
	}

	// �ϳ��� �Խñ۸� ������ �� ���
	public int deleteBoard(long id, int category) {
		long[] onlyone = new long[1];
		onlyone[0] = id;

		return deleteBoard(onlyone, category);
	}

	// üũ�ڽ��� ���� ���� ���� �������� ���� �����ؾ� �� �� ���
	public int deleteBoard(long[] dids, int category) {
		int result = 0;
		String didsForDB = "";
		for (int i = 0; i < dids.length; i++) {
			if (i < dids.length - 1) {
				didsForDB += dids[i];
				didsForDB += ",";
			} else
				didsForDB += dids[i];
		}

		String sql = "delete from board where id in(" + didsForDB + ") and category = ?;";

		// �ش� �Խñ��� ��� ��۰� ���� ����
		String delAllCmt = "delete from board_comment where board_id in(" + didsForDB + ") and category = ?;";
		String delAllReCmt = "delete from re_comment where board_id in(" + didsForDB + ") and category = ?;";
		Connection conn = DatabaseUtil.getConnection();
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, category);
			result += pstmt.executeUpdate();

			PreparedStatement pstmt2 = conn.prepareStatement(delAllCmt);
			pstmt2.setInt(1, category);
			pstmt2.executeUpdate();

			PreparedStatement pstmt3 = conn.prepareStatement(delAllReCmt);
			pstmt3.setInt(1, category);
			pstmt3.executeUpdate();
			conn.commit();

			pstmt.close();
			pstmt2.close();
			pstmt3.close();
		} catch (SQLException e) {
			result = 0;
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
	}

	// 1���� �Խñ� ����, ����� ����
	public int updateUrgBoard(Board board) {
		int result = 0; // ������Ʈ �Ϸ�� Ʃ�� ���� ����
		String sql = "update board set urg = ? where id = ? and category = ?;";
		Connection conn = DatabaseUtil.getConnection();

		try {// ���� ����� -1�� ���Ƿ� ���� ���� ó�� �� ���൵ ��.
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setBoolean(1, board.isUrg());
			pstmt.setLong(2, board.getId());
			pstmt.setInt(3, board.getCategory());
			result = pstmt.executeUpdate();
			conn.commit();
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; // ������Ʈ�� �� Ʃ�� ���� ��ȯ.
		}
	}

	public int updateUrgInBoard(List<String> uids, List<String> nids, int category) {
		String uidsJoin = String.join(",", uids); // ����Ʈ�� ��Ƴ��� id���� DB�� ������ �� �ֵ��� 1,2,3... ���� ��������
		String nidsJoin = String.join(",", nids);

		int result = 0; // ������Ʈ �Ϸ�� Ʃ�� ���� ����

		String updateUrgent = "update board set urg = true where id in(" + uidsJoin + ") and category = ?;";
		String updateNormal = "update board set urg = false where id in(" + nidsJoin + ") and category = ?;";
		Connection conn = DatabaseUtil.getConnection();

		try {// ��������� -1�� ���� �ǹǷ� ���� ���� ó�� �� ���൵ ��.
			conn.setAutoCommit(false);
			PreparedStatement urgent = conn.prepareStatement(updateUrgent);
			urgent.setInt(1, category);
			result += urgent.executeUpdate();

			if (!nidsJoin.equals("")) {
				PreparedStatement normal = conn.prepareStatement(updateNormal);
				normal.setInt(1, category);
				result += normal.executeUpdate();
				normal.close();
			}
			conn.commit();
			urgent.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; // ������Ʈ�� �� Ʃ�� ���� ��ȯ.
		}
	}

	public int modifyCmt(CmtBody cmt, boolean admin) {
		int result = 0;
		String updateCmt = "update board_comment set content = ?, ip = ? where id = ? and board_id = ? and category = ?;";
		String updateRcmt = "update re_comment set content = ?, ip = ? where id = ? and cmt_id = ? and board_id = ? and category = ?;";
		Connection conn = DatabaseUtil.getConnection();

		try {// ���� ����� -1�� ���Ƿ� ���� ���� ó�� �� ���൵ ��.
			conn.setAutoCommit(false);
			PreparedStatement pstmt = null;
			if (cmt instanceof Comment) {
				pstmt = conn.prepareStatement(updateCmt);
				pstmt.setString(1, ((Comment) cmt).getContent());
				pstmt.setString(2, ((Comment) cmt).getWriterIp());
				pstmt.setLong(3, ((Comment) cmt).getId());
				pstmt.setLong(4, ((Comment) cmt).getBoardId());
				pstmt.setInt(5, ((Comment) cmt).getCategory());
				result = pstmt.executeUpdate();
			} else if (cmt instanceof ReComment) {
				pstmt = conn.prepareStatement(updateRcmt);
				pstmt.setString(1, ((ReComment) cmt).getContent());
				pstmt.setString(2, ((ReComment) cmt).getWriterIp());
				pstmt.setLong(3, ((ReComment) cmt).getId());
				pstmt.setLong(4, ((ReComment) cmt).getCmtId());
				pstmt.setLong(5, ((ReComment) cmt).getBoardId());
				pstmt.setInt(6, ((ReComment) cmt).getCategory());
				result = pstmt.executeUpdate();
			}
			conn.commit();
			pstmt.close();
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; // ������Ʈ�� �� Ʃ�� ���� ��ȯ.
		}
	}

	public int modifyBoard(Board board) {
		int result = 0;
		String sql = "update board set title= ?, content = ?, files = ?, ip = ? where id = ? and category = ?;";
		Connection conn = DatabaseUtil.getConnection();

		try {// ���� ����� -1�� ���Ƿ� ���� ���� ó�� �� ���൵ ��.
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, board.getTitle());
			pstmt.setString(2, board.getContent());
			pstmt.setString(3, board.getFiles());
			pstmt.setString(4, board.getWriterIp());
			pstmt.setLong(5, board.getId());
			pstmt.setInt(6, board.getCategory());

			result = pstmt.executeUpdate();

			conn.commit();
			pstmt.close();
		} catch (

		SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; // ������Ʈ�� �� Ʃ�� ���� ��ȯ.
		}
	}

	// �Խñ� ��� �������� ����.
	public List<Comment> getBoardComment(long boardId, int category, int page) {
		List<Comment> list = new ArrayList<>();
		List<ReComment> rList = new ArrayList<>();
		// sql�� ���� �� ���� �ϳ��ϳ� ��������.
		String sql = "select * from(select *, ROW_NUMBER() over(order by regdate asc) as rnum from board_comment where board_id = ? and category = ?)N where rnum between ? and ?";
		String rCmtSql = "select * from re_comment where cmt_id = ? and board_id = ? and category = ? order by regdate asc";

		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection conn = DatabaseUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, boardId);
			pstmt.setInt(2, category);
			pstmt.setInt(3, 1 + (page - 1) * 10); // ���޵� �������� ���� ��ȣ, �� �������� 10���� �������� ���´�.
			pstmt.setInt(4, page * 10);// �������� �� ��ȣ, 1~10 / 11~20 ...
			rs = pstmt.executeQuery();

			while (rs.next()) {
				long id = rs.getLong("id");
				String nickname = rs.getString("nickname");
				String content = rs.getString("content");
				String regdate = rs.getString("regdate");
				String writerId = rs.getString("writer");
				String writerIp = rs.getString("ip");
				boolean del = rs.getBoolean("del");

				pstmt2 = conn.prepareStatement(rCmtSql);
				pstmt2.setLong(1, id);
				pstmt2.setLong(2, boardId);
				pstmt2.setInt(3, category);
				rs2 = pstmt2.executeQuery();

				while (rs2.next()) {
					long rId = rs2.getLong("id");
					String rNickname = rs2.getString("nickname");
					String rContent = rs2.getString("content");
					String rRegdate = rs2.getString("regdate");
					String rWriterId = rs2.getString("writer");
					String rWriterIp = rs2.getString("ip");
					long targetCmt = rs2.getLong("cmt_id");
					boolean del2 = rs2.getBoolean("del");
					rList.add(new ReComment(rId, rNickname, rContent, rRegdate, rWriterId, rWriterIp, targetCmt, del2));
				}

				Comment cm = new Comment(id, nickname, content, regdate, writerId, writerIp, rList, del);
				list.add(cm);
			}
			rs.close();
			rs2.close();
			pstmt.close();
			pstmt2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return list;
		}
	}

	public Comment getCommentForReport(int category, long boardId, long cmtId) {
		Comment c = null;
		String sql = "select * from board_comment where category = ? and board_id = ? and id = ?;";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = DatabaseUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, category);
			pstmt.setLong(2, boardId);
			pstmt.setLong(3, cmtId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				c = new Comment();
				c.setCategory(category);
				c.setBoardId(boardId);
				c.setId(cmtId);
				c.setContent(rs.getString("content"));
				c.setDel(rs.getBoolean("del"));
				c.setNickname(rs.getString("nickname"));
				c.setWriterId(rs.getString("writer"));
				c.setWriterIp(rs.getString("ip"));
				c.setRegdate(rs.getString("regdate"));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return c;
		}
	}
	public ReComment getReCommentForReport(int category, long boardId, long cmtId, long rCmtId) {
		ReComment rc = null;
		String sql = "select * from re_comment where category = ? and board_id = ? and cmt_id = ? and id = ?;";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = DatabaseUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, category);
			pstmt.setLong(2, boardId);
			pstmt.setLong(3, cmtId);
			pstmt.setLong(4, rCmtId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				rc = new ReComment();
				rc.setCategory(category);
				rc.setBoardId(boardId);
				rc.setCmtId(cmtId);
				rc.setId(rCmtId);
				rc.setContent(rs.getString("content"));
				rc.setDel(rs.getBoolean("del"));
				rc.setNickname(rs.getString("nickname"));
				rc.setWriterId(rs.getString("writer"));
				rc.setWriterIp(rs.getString("ip"));
				rc.setRegdate(rs.getString("regdate"));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return rc;
		}
	}
	
	//�Խ��� ���� �� ���� ����
	
	//�θ� �Խ��� �����ϱ�
	public int regParentBoard(ParentBoardInfo pbi) {
		String sql = "insert into parent_board_info(category, name) values(?,?);";
		String findEmptyCategory = "select max(category) as category from parent_board_info;";
		int result = 0;
		Connection conn = DatabaseUtil.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(findEmptyCategory);
			int next = 1;
			if (rs.next())
				next = rs.getInt("category") + 1;

			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, next);
			pstmt.setString(2, pbi.getName());
			result = pstmt.executeUpdate();
			conn.commit();
			pstmt.close();
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			result = 0;
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return result;
		}
	}
	
	//����(�ڽ�) �Խ��� �����ϱ�
	public int regChildBoardInfo(ChildBoardInfo cbi) {
		String sql = "insert into child_board_info(category, name, kind, parent, admin, cmt_permit, read_permit) values(?,?,?,?,?,?,?);";
		String findEmptyCategory = "select max(category) as category from child_board_info;";
		int next = 1;
		int result = 0;
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement find = conn.prepareStatement(findEmptyCategory);
			ResultSet rs = find.executeQuery();
			if (rs.next())
				next = rs.getInt("category") + 1;

			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, next);
			pstmt.setString(2, cbi.getName());
			pstmt.setInt(3, cbi.getKind());
			pstmt.setInt(4, cbi.getParent());
			pstmt.setBoolean(5, cbi.getAdmin());
			pstmt.setBoolean(6, cbi.getCmtPermit());
			pstmt.setBoolean(7, cbi.getReadPermit());
			
			result = pstmt.executeUpdate();
			conn.commit();
			pstmt.close();
		} catch (SQLException e) {
			result = -1;
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return result;
		}
	}
	
	//�� �������� ��Ӵٿ� ����Ʈ�� �����ϱ� ���� �޼���. 
	public List<ParentBoardInfo> getParentBoardInfo() {
		String sql = "select * from parent_board_info order by category asc;";
		List<ParentBoardInfo> list = new ArrayList<>();
		ParentBoardInfo pbi = null;
		Connection conn = DatabaseUtil.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				// �ڽ� �Խ��� ���� ��������
				pbi = new ParentBoardInfo();
				pbi.setCategory(rs.getInt("category"));
				pbi.setName(rs.getString("name"));
				pbi.setChildren(getChildrenBoardInfo(pbi.getCategory()));
				list.add(pbi);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return list;
		}
	}
	
	//���� ��Ӵٿ� ����Ʈ�� �����ϱ� ���� �θ� ����Ʈ �������⿡��, �� �θ𸶴� �Ҵ�� �ڽ� �Խ����� ���� �޾��ִ� �޼���
	//1�� �θ� �Խ����� 1,2,3,4���� �ڽ� �Խ����� ���� �ִٸ� ���� ��������� �Ѵ�.
	public List<ChildBoardInfo> getChildrenBoardInfo(int parent) {
		String getChildren = "select * from child_board_info where parent = ?;";
		ChildBoardInfo cbi = null;
		List<ChildBoardInfo> list = new ArrayList<>();
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(getChildren);
			pstmt.setInt(1, parent);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				cbi = new ChildBoardInfo();
				cbi.setCategory(rs.getInt("category"));
				cbi.setKind(rs.getInt("kind"));
				cbi.setName(rs.getString("name"));
				cbi.setParent(rs.getInt("parent"));
				cbi.setAdmin(rs.getBoolean("admin"));
				cbi.setCmtPermit(rs.getBoolean("cmt_permit"));
				cbi.setReadPermit(rs.getBoolean("read_permit"));

				list.add(cbi);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			list = null;
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return list;
		}
	}
	//����ڰ� ���� �Խ����� Ŭ������ ���, ����Ʈ ��Ʈ�ѷ����� �ش� �Խ��ǿ� ���� ������ ��Ƽ� ����� �������� �־���� �Ѵ�.
	//�̶� ����� �޼ҵ��̴�. 
	public ChildBoardInfo getChildBoardInfo(int category) {
		String getChild = "select * from child_board_info where category = ?;";
		ChildBoardInfo cbi = null;
		Connection conn = DatabaseUtil.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(getChild);
			pstmt.setInt(1, category);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				cbi = new ChildBoardInfo();
				cbi.setCategory(rs.getInt("category"));
				cbi.setKind(rs.getInt("kind"));
				cbi.setName(rs.getString("name"));
				cbi.setParent(rs.getInt("parent"));
				cbi.setAdmin(rs.getBoolean("admin"));
				cbi.setCmtPermit(rs.getBoolean("cmt_permit"));
				cbi.setReadPermit(rs.getBoolean("read_permit"));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			cbi = null;
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			return cbi;
		}
	}
	
	//�θ� �Խ��� ���� ����, ����� �켱 ���� ������ ����. �ܼ� �̸� ���游 
	public int modParentBoardInfo(ParentBoardInfo pbi) {
		String sql = "update parent_board_info set name = ? where category = ?;";
		Connection conn = DatabaseUtil.getConnection();
		int result = 0;
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pbi.getName());
			pstmt.setInt(2, pbi.getCategory());
			result = pstmt.executeUpdate();

			conn.commit();
			pstmt.close();
		} catch (SQLException e) {
			result = -1;
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result; 
		}
	}
	//�ڽ� �Խ��� ���� ����, �Ŀ� �� / ��� �ۼ� ����, �̷α��� ����� �б� ���� � ������ �� �ְ� �ٲ� ��.
		public int modChildBoardInfo(ChildBoardInfo cbi) {
			String sql = "update child_board_info set name = ? where category = ?;";
			Connection conn = DatabaseUtil.getConnection();
			int result = 0;
			try {
				conn.setAutoCommit(false);
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, cbi.getName());
				pstmt.setInt(2, cbi.getCategory());
				result = pstmt.executeUpdate();

				conn.commit();
				pstmt.close();
			} catch (SQLException e) {
				result = -1;
				if (conn != null)
					try {
						conn.rollback();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				e.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return result; 
			}
		}
	
	
	//�θ� �Խ��� �� �ڽ� �Խ��� ����, ������ �Խñ��� board��� ǥ���ߴµ�, ���� Post�� ����. ���� �� ����
	
	//�� �޼ҵ忡�� Ȥ�� �� �޼ҵ带 ȣ���� ������ �ݵ�� ������ �Խñۿ� ���ε�� ������ �ű�� �۾� �߰�
	public int delAllPostAndCmt(String categories) {
		String delAllPost = "delete from board where category in(" + categories + ")";
		String delAllCmt = "delete from board_comment where category in(" + categories + ")";
		String delAllReCmt = "delete from re_comment where category in(" + categories + ")";
		int result = 0;
		Connection conn = DatabaseUtil.getConnection();
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(delAllPost);
			result += pstmt.executeUpdate();
			PreparedStatement pstmt2 = conn.prepareStatement(delAllCmt);
			result +=pstmt2.executeUpdate();

			PreparedStatement pstmt3 = conn.prepareStatement(delAllReCmt);
			result +=pstmt3.executeUpdate();
			conn.commit();
			
			conn.commit();
			pstmt.close();
			pstmt2.close();
			pstmt3.close();
		} catch (SQLException e) {
			result = -1;
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	
	public int delChildBoardInfo(int category) {
		String category_ = String.valueOf(category);
		return delChildBoardInfo(category_);
	}
	
	public int delChildBoardInfo(String categories) {
		String delCbi = "delete from child_board_info where category in(" + categories + ");";
		int result = 0;
		
		Connection conn = DatabaseUtil.getConnection();
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(delCbi);
			delAllPostAndCmt(categories);//�ش� �ڽ� �Խ����� ��� ������ ����, ���� ��ƿ�� �̿��Ͽ� �����Ǵ� �Խ��ǿ� ���ε�� ������ ������ ��� ���������� �ű�� �۾� �߰��� ��.
			result = pstmt.executeUpdate();
			conn.commit();

			pstmt.close();
		} catch (SQLException e) {
			result = 0;
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	
	public int delParentBoardInfo(int category) {
		String delPbi = "delete from parent_board_info where category = ?;";
		List<ChildBoardInfo> targetList = getChildrenBoardInfo(category);//�θ� �Խ����� �����Ǹ� �ڽ� �Խ��ǵ� ��� �����ؾ� ��.
		StringBuilder plate = new StringBuilder();
		int result = 0;
		
		for(ChildBoardInfo cbi : targetList) 
			plate.append(cbi.getCategory()+",");
		plate.setLength(plate.length()-1);
		
		Connection conn = DatabaseUtil.getConnection();
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(delPbi);
			pstmt.setInt(1, category);
			delChildBoardInfo(plate.toString());//�ش� �θ� �Խ��ǿ� ���� �ִ� �ڽ� �Խ����� ��� ������ ����. �� �޼ҵ� �ȿ��� �Խñ�, ��� ���� ���� ��� ����.
			result = pstmt.executeUpdate();
			conn.commit();

			pstmt.close();
		} catch (SQLException e) {
			result = 0;
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
}

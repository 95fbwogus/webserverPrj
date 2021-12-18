package entity;

public class BoardView extends Board {
	private int cmtCount;
	public int getCmtCount() {
		return cmtCount;
	}
	public void setCmtCount(int cmtCount) {
		this.cmtCount = cmtCount;
	}

	public BoardView() {};

	public BoardView(String title, String nickname, String files, boolean pub, int cmtCount, int reCmtCount,boolean urg) {
		super(title, nickname, files, "", pub, urg); //������ �Ѱܹ��� �ʾ����Ƿ�	�� ���ڿ�, �⺻������ ��ü
		this.cmtCount = cmtCount;//�ܼ� ����Ʈ ��¿�
	}
	
	//getBoardList�� ���� ������.
	public BoardView(Long id, String title, String nickname, String regdate, int hit, String files, boolean pub,
			int cmtCount, boolean urg) {
		super(id, title, nickname, regdate, hit, files, "", pub, urg); // ������ �Ѱܹ��� �ʾ����Ƿ�
		this.cmtCount = cmtCount;	            	                // �� ���ڿ�, �⺻������ ��ü
	}
}

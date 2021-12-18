package entity;

public class ChildBoardInfo {
	private int category;
	private int kind;//�Խ����� ī�� ��������, �Ϲ� �Խ��� ��������
	private String name;
	private int parent;
	private boolean admin;//���� ������ - �Ϲ� ����� �Խ����� ������ ���� ���
	private boolean cmtPermit;//��� ���
	private boolean readPermit;//ȸ���� �б� ���
	
	public ChildBoardInfo() {}
	
	public ChildBoardInfo(int category, int kind, String name, int parent, boolean admin, boolean cmtPermit,
			boolean readPermit) {
		this.category = category;
		this.kind = kind;
		this.name = name;
		this.parent = parent;
		this.admin = admin;
		this.cmtPermit = cmtPermit;
		this.readPermit = readPermit;
	}
	
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public int getKind() {
		return kind;
	}
	public void setKind(int kind) {
		this.kind = kind;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getParent() {
		return parent;
	}
	public void setParent(int parent) {
		this.parent = parent;
	}
	public boolean getAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public boolean getCmtPermit() {
		return cmtPermit;
	}
	public void setCmtPermit(boolean cmtPermit) {
		this.cmtPermit = cmtPermit;
	}
	public boolean getReadPermit() {
		return readPermit;
	}
	public void setReadPermit(boolean readPermit) {
		this.readPermit = readPermit;
	}

	@Override
	public String toString() {
		return "ChildBoardInfo [category=" + category + ", kind=" + kind + ", name=" + name + ", parent=" + parent
				+ ", admin=" + admin + ", cmtPermit=" + cmtPermit + ", readPermit=" + readPermit + "]";
	}

}

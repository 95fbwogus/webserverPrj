package entity;
//folderName 추가 2021-12-19
public class ChildBoardInfo {
	private int category;
	private int kind;// 게시판이 카드 형식인지, 일반 게시판 형식인지
	private String name;
	private int parent;
	private boolean admin;// 이후 관리자 - 일반 사용자 게시판이 합쳐질 것을 대비
	private boolean cmtPermit;// 댓글 허용
	private boolean readPermit;// 회원만 읽기 허용
	private String folderName;

	public ChildBoardInfo() {
	}
	
	public ChildBoardInfo(int category, int kind, String name, int parent, boolean admin, boolean cmtPermit,
			boolean readPermit, String folderName) {
		this.category = category;
		this.kind = kind;
		this.name = name;
		this.parent = parent;
		this.admin = admin;
		this.cmtPermit = cmtPermit;
		this.readPermit = readPermit;
		this.folderName = folderName;
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

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	@Override
	public String toString() {
		return "ChildBoardInfo [category=" + category + ", kind=" + kind + ", name=" + name + ", parent=" + parent
				+ ", admin=" + admin + ", cmtPermit=" + cmtPermit + ", readPermit=" + readPermit + "]";
	}

}

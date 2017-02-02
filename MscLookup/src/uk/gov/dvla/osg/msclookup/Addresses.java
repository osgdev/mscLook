package uk.gov.dvla.osg.msclookup;

public class Addresses {

	private int sequence;
	private String docref, add1, add2, add3, add4, add5, pc, msc, dps;
	
	public Addresses(int sequence, String docref, String add1, String add2, String add3, String add4, String add5, String pc){
		this.sequence=sequence;
		this.docref=docref;
		this.add1=add1;
		this.add2=add2;
		this.add3=add3;
		this.add4=add4;
		this.add5=add5;
		this.pc=pc;
	}

	public int getSequence(){
		return sequence;
	}
	
	public void setSequence(int sequence){
		this.sequence = sequence;
	}
	
	public String getAdd1() {
		return add1;
	}

	public void setAdd1(String add1) {
		this.add1 = add1;
	}

	public String getAdd2() {
		return add2;
	}

	public void setAdd2(String add2) {
		this.add2 = add2;
	}

	public String getAdd3() {
		return add3;
	}

	public void setAdd3(String add3) {
		this.add3 = add3;
	}

	public String getAdd4() {
		return add4;
	}

	public void setAdd4(String add4) {
		this.add4 = add4;
	}

	public String getAdd5() {
		return add5;
	}

	public void setAdd5(String add5) {
		this.add5 = add5;
	}

	public String getPc() {
		return pc;
	}

	public void setPc(String pc) {
		this.pc = pc;
	}

	public String getMsc() {
		return msc;
	}

	public void setMsc(String msc) {
		this.msc = msc;
	}

	public String getDps() {
		return dps;
	}

	public void setDps(String dps) {
		this.dps = dps;
	}

	@Override
	public String toString() {
		return sequence + "|" + add1 + "|" + add2 + "|" + add3
				+ "|" + add4 + "|" + add5 + "|" + pc;
	}
	
	public String[] print(){
		String[] result = new String[] {docref,msc,dps};
		return result;
	}
}

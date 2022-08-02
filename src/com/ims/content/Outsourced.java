package com.ims.content;

public class Outsourced extends Part {

	private String companyName;

	public Outsourced() {
		super();
	}

	/**
	 * Get company name of outsourced part
	 * @return companyName
	 */
	public String getCompanyName() {
		return companyName;
	}

	/**
	 * Set company name of outsourced part
	 * @param companyName
	 */
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}

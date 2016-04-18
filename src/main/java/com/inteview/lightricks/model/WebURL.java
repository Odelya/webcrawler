/**
 * 
 */
package com.inteview.lightricks.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

/**
 * @author Odelya Holiday
 *
 *        
 */
@Entity
public class WebURL {

	@Id
	String url;

	Integer depth;

	Float ratio;

	Long updated;

	@Column(columnDefinition = "TINYINT")
	@Type(type = "org.hibernate.type.NumericBooleanType")
	Boolean process;

	@Transient
	List<String> links;

	public WebURL(String pageRoot, int depth) {
		this.url = pageRoot;
		this.depth = depth;
	}

	public WebURL(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	public Float getRatio() {
		return ratio;
	}

	public void setRatio(Float ratio) {
		this.ratio = ratio;
	}

	public List<String> getLinks() {
		if (this.links == null)
			links = new ArrayList<String>();
		return links;
	}

	public void setLinks(List<String> links) {
		this.links = links;
	}

	public Long getUpdated() {
		return updated;
	}

	public void setUpdated(Long updated) {
		this.updated = updated;
	}

	public Boolean getProcess() {
		return process;
	}

	public void setProcess(Boolean process) {
		this.process = process;
	}

	public Object[] toArrayOuput() {
		Object[] result = new Object[3];
		result[0] = this.getUrl();
		result[1] = this.getDepth();
		result[2] = this.getRatio();
		return result;
	}

	public WebURL() {
	}

}

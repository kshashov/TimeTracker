package com.github.kshashov.timetracker.web.mvc.components.detail;

import com.github.kshashov.timetracker.web.mvc.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.mvc.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.mvc.views.MasterDetail;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport("frontend://styles/components/detail.css")
public class Detail extends FlexBoxLayout {

	private static final String CLASS_NAME = "detail";

	private FlexBoxLayout header;
	private FlexBoxLayout content;
	private FlexBoxLayout footer;

	public Detail(MasterDetail.Position position, Component... components) {
		setClassName(CLASS_NAME);
		setPosition(position);

		header = new FlexBoxLayout();
		header.setClassName(CLASS_NAME + "__header");

		content = new FlexBoxLayout(components);
		content.setClassName(CLASS_NAME + "__content");
		content.setFlexDirection(FlexDirection.COLUMN);

		footer = new FlexBoxLayout();
		footer.setClassName(CLASS_NAME + "__footer");

		add(header, content, footer);
	}

	public void setHeader(Component... components) {
		this.header.removeAll();
		this.header.add(components);
	}

	public FlexBoxLayout getHeader() {
		return this.header;
	}

	public void setContent(Component... components) {
		this.content.removeAll();
		this.content.add(components);
	}

	public void setFooter(Component... components) {
		this.footer.removeAll();
		this.footer.add(components);
	}

	public void setPosition(MasterDetail.Position position) {
		getElement().setAttribute("position", position.name().toLowerCase());
	}

	public void collapse() {
		getElement().setAttribute("expand", false);
	}

	public void expand() {
		getElement().setAttribute("expand", true);
	}
}

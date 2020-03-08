package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.detail.Detail;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;

/**
 * A view frame that establishes app design guidelines. It consists of four
 * parts:
 * <ul>
 * <li>Topmost {@link #setViewHeader(Component...) header}</li>
 * <li>Center {@link #setViewContent(Component...) content}</li>
 * <li>Bottom {@link #setViewFooter(Component...) footer}</li>
 * </ul>
 */
@CssImport("./styles/components/master-detail.css")
public class MasterDetail extends Composite<Div> implements HasStyle {

	private static final String CLASS_NAME = "master-detail";

	private Div header;

	private FlexBoxLayout wrapper;
	private Div content;
	private Detail details;

	private Div footer;

	public enum Position {
		RIGHT, BOTTOM
	}

	public enum Size {
		SMALL, NORMAL, LARGE
	}

	public MasterDetail(Position position) {
		setClassName(CLASS_NAME);

		header = new Div();
		header.setClassName(CLASS_NAME + "__header");

		wrapper = new FlexBoxLayout();
		wrapper.setClassName(CLASS_NAME + "__wrapper");

		content = new Div();
		content.setClassName(CLASS_NAME + "__content");

		details = new Detail(position);
		details.addClassName(CLASS_NAME + "__details");

		footer = new Div();
		footer.setClassName(CLASS_NAME + "__footer");

		wrapper.add(content, details);
		getContent().add(header, wrapper, footer);

		setViewDetailsPosition(position);
		setDetailSize(Size.NORMAL);
	}

	/**
	 * Sets the header slot's components.
	 */
	public void setViewHeader(Component... components) {
		header.removeAll();
		header.add(components);
	}

	/**
	 * Sets the content slot's components.
	 */
	public void setViewContent(Component... components) {
		content.removeAll();
		content.add(components);
	}

	public void setDetailSize(Size size) {
		details.getElement().setAttribute("size", size.name().toLowerCase());
	}

	public Detail getDetailsDrawer() {
		return details;
	}

	private void setViewDetailsPosition(Position position) {
		if (position.equals(Position.RIGHT)) {
			wrapper.setFlexDirection(FlexDirection.ROW);

		} else if (position.equals(Position.BOTTOM)) {
			wrapper.setFlexDirection(FlexDirection.COLUMN);
		}
	}

	/**
	 * Sets the footer slot's components.
	 */
	public void setViewFooter(Component... components) {
		footer.removeAll();
		footer.add(components);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		MainLayout.get().getAppBar().reset();
	}
}

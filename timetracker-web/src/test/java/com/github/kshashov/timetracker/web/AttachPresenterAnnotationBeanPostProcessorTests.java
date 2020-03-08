package com.github.kshashov.timetracker.web;

import com.github.kshashov.timetracker.web.ui.mvp.AttachPresenter;
import com.github.kshashov.timetracker.web.ui.mvp.Presenter;
import com.github.kshashov.timetracker.web.ui.mvp.View;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ContextConfiguration(classes = {AttachPresenterAnnotationBeanPostProcessorTests.ViewConfiguration.class})
public class AttachPresenterAnnotationBeanPostProcessorTests extends BaseIntegrationTest {
    @TestConfiguration
    static class ViewConfiguration {

        @Bean
        @Scope("prototype")
        GenericPresenter<Integer> genericPresenter() {
            return new GenericPresenter<>();
        }

        @Bean
        @Scope("prototype")
        UniversalPresenter universalPresenter() {
            return new UniversalPresenter();
        }

        @Bean
        @Scope("prototype")
        MyPresenter myPresenter() {
            return new MyPresenter();
        }

        @Bean("genericPresenterTypeView")
        @Scope("prototype")
        ViewContract genericPresenterTypeView(ApplicationContext context) {
            return new GenericPresenterView();
        }

        @Bean("workingView")
        @Scope("prototype")
        ViewContract workingView(ApplicationContext context) {
            return new WorkingView(context);
        }

        @Bean
        @Scope("prototype")
        ViewContract workingComponentView(ApplicationContext context) {
            return new WorkingComponentView(context);
        }

        @Bean
        @Scope("prototype")
        ViewContract missingViewInterfaceView() {
            return new MissingViewInterfaceView();
        }

        @Bean
        @Scope("prototype")
        ViewContract rawViewInterfaceView() {
            return new RawViewInterfaceView();
        }

        @Bean
        @Scope("prototype")
        ViewContract missingPresenterView() {
            return new MissingPresenterView();
        }

        @Bean
        @Scope("prototype")
        ViewContract finalComponentView() {
            return new FinalComponentView();
        }

        @Bean
        @Scope("prototype")
        ViewContract finalView() {
            return new FinalView();
        }

        @Bean
        @Scope("prototype")
        ViewContract incompatiblePresenterTypeView() {
            return new IncompatiblePresenterTypeView();
        }
    }

    @Autowired
    ApplicationContext context;

    @Test
    public void getBean_WithoutComponent_PresenterInjected() {
        Object bean = context.getBean("workingView");

        // Check contract
        assertThat(bean).isInstanceOf(ViewContract.class);
        assertThat(bean).isInstanceOf(View.class);
        assertThat(bean).isInstanceOf(WorkingView.class);

        WorkingView view = (WorkingView) bean;

        // Check autowired constructor dependencies
        assertThat(view.getParam()).isNotNull();

        // Check presenter
        UniversalPresenter myPresenter = view.getPresenter();
        assertThat(myPresenter).isNotNull();
    }

    @Test
    public void getBean_ComponentView_PresenterInjectedAttachedDetached() {
        Object bean = context.getBean("workingComponentView");

        // Check contract
        assertThat(bean).isInstanceOf(ViewContract.class);
        assertThat(bean).isInstanceOf(View.class);
        assertThat(bean).isInstanceOf(WorkingComponentView.class);

        WorkingComponentView view = (WorkingComponentView) bean;

        // Check autowired constructor dependencies
        assertThat(view.getParam()).isNotNull();

        // Check presenter
        MyPresenter myPresenter = view.getPresenter();
        assertThat(myPresenter).isNotNull();
        assertThat(myPresenter.getView()).isNull();
        assertThat(myPresenter.getAttached()).isEqualTo(0);
        assertThat(myPresenter.getDetached()).isEqualTo(0);

        // On vaadin component attach
        view.onAttach(null);
        assertThat(myPresenter.getView()).isEqualTo(view);
        assertThat(myPresenter.getAttached()).isEqualTo(1);
        assertThat(myPresenter.getDetached()).isEqualTo(0);

        // On vaadin component detach
        view.onDetach(null);
        assertThat(myPresenter.getView()).isNull();
        assertThat(myPresenter.getAttached()).isEqualTo(1);
        assertThat(myPresenter.getDetached()).isEqualTo(1);
    }

    @Test
    public void getBean_MissingViewInterface_ExceptionThrown() {
        assertThrows(
                BeanCreationException.class,
                () -> context.getBean("missingViewInterfaceView"));
    }

    @Test
    public void getBean_RawViewInterface_ExceptionThrown() {
        assertThrows(
                BeanCreationException.class,
                () -> context.getBean("rawViewInterfaceView"));
    }

    @Test
    public void getBean_MissingPresenter_ExceptionThrown() {
        assertThrows(
                BeanCreationException.class,
                () -> context.getBean("missingPresenterView"));
    }

    @Test
    public void getBean_FinalView_ExceptionThrown() {
        assertThrows(
                BeanCreationException.class,
                () -> context.getBean("finalView"));
    }

    @Test
    public void getBean_FinalComponentView_ExceptionThrown() {
        assertThrows(
                BeanCreationException.class,
                () -> context.getBean("finalComponentView"));
    }

    @Test
    public void getBean_IncompatiblePresenterType_ExceptionThrown() {
        assertThrows(
                BeanCreationException.class,
                () -> context.getBean("incompatiblePresenterTypeView"));
    }

    @Test
    public void getBean_GenericPresenterType_PresenterInjected() {
        Object bean = context.getBean("genericPresenterTypeView");

        // Check contract
        assertThat(bean).isInstanceOf(ViewContract.class);
        assertThat(bean).isInstanceOf(View.class);
        assertThat(bean).isInstanceOf(GenericPresenterView.class);

        GenericPresenterView view = (GenericPresenterView) bean;

        // Check presenter
        GenericPresenter<Integer> myPresenter = view.getPresenter();
        assertThat(myPresenter).isNotNull();
    }

    interface ViewContract {
    }

    static class OpenedComponent extends Component {
        @Override
        public void onAttach(AttachEvent attachEvent) { // open for testing purposes
            super.onAttach(attachEvent);
        }

        @Override
        public void onDetach(DetachEvent detachEvent) { // open for testing purposes
            super.onDetach(detachEvent);
        }
    }

    @Tag("div")
    @AttachPresenter
    static final class IncompatiblePresenterTypeView extends Component implements View<MyPresenter>, ViewContract {
    }

    @AttachPresenter
    static final class FinalView implements View<UniversalPresenter>, ViewContract {
    }

    @Tag("div")
    @AttachPresenter
    static final class FinalComponentView extends Component implements View<UniversalPresenter>, ViewContract {
    }

    @Getter
    @Tag("div")
    @AttachPresenter
    static class MissingPresenterView extends Component implements View<MissingPresenter>, ViewContract {
    }

    @Getter
    @Tag("div")
    @AttachPresenter
    static class RawViewInterfaceView extends Component implements View, ViewContract {
    }

    @Getter
    @Tag("div")
    @AttachPresenter
    static class MissingViewInterfaceView extends Component implements ViewContract {
    }

    @Getter
    @Tag("div")
    @AttachPresenter
    @SpringComponent
    static class GenericPresenterView extends OpenedComponent implements ViewContract, View<GenericPresenter<Integer>> {
    }

    @Getter
    @Tag("div")
    @AttachPresenter
    @SpringComponent
    static class WorkingView implements ViewContract, View<UniversalPresenter> {
        private ApplicationContext param;

        @Autowired
        public WorkingView(ApplicationContext param) {
            this.param = param;
        }
    }

    @Getter
    @Tag("div")
    @AttachPresenter
    @SpringComponent
    static class WorkingComponentView extends OpenedComponent implements ViewContract, View<MyPresenter> {
        private ApplicationContext param;

        @Autowired
        public WorkingComponentView(ApplicationContext param) {
            this.param = param;
        }
    }

    static class MissingPresenter implements Presenter<View<?>> {
    }

    @Getter
    @SpringComponent
    static class UniversalPresenter implements Presenter<View<?>> {

    }

    @Getter
    @SpringComponent
    static class GenericPresenter<T> implements Presenter<View<?>> {
        private int attached = 0;
        private int detached = 0;
        private View<?> view;

        @Override
        public void onAttach(View<?> view) {
            this.view = view;
            attached++;
        }

        @Override
        public void onDetach() {
            this.view = null;
            detached++;
        }
    }

    @Getter
    @SpringComponent
    static class MyPresenter implements Presenter<WorkingComponentView> {
        private int attached = 0;
        private int detached = 0;
        private WorkingComponentView view;

        @Override
        public void onAttach(WorkingComponentView view) {
            this.view = view;
            attached++;
        }

        @Override
        public void onDetach() {
            this.view = null;
            detached++;
        }
    }
}

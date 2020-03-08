package com.github.kshashov.timetracker.web.ui.mvp;

import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.component.Component;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class AttachPresenterAnnotationBeanPostProcessor implements BeanPostProcessor {
    private final ListableBeanFactory beanFactory;

    @Autowired
    public AttachPresenterAnnotationBeanPostProcessor(BeanFactory beanFactory) {
        Assert.isInstanceOf(ListableBeanFactory.class, beanFactory,
                "AttachPresenterAnnotationBeanPostProcessor can only be used with a ListableBeanFactory");
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Check for ParameterizedType annotation
        AttachPresenter annotation = bean.getClass().getAnnotation(AttachPresenter.class);
        if (annotation != null) {
            // Check for View interface
            Type viewType = GenericTypeReflector.getExactSuperType(bean.getClass(), View.class);
            Assert.notNull(viewType, String.format("'%s' class doesn't implement '%s' interface", bean.getClass(), View.class));
            Assert.isAssignable(ParameterizedType.class, viewType.getClass(), String.format("'%s' class implement raw '%s' type that makes it impossible to search for a presenter", bean.getClass(), View.class));
            if (viewType instanceof ParameterizedType) {
                return processView((View<?>) bean);
            }
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    protected Object processView(View<?> view) {
        // Get parameter for View<> interface
        Type presenterType = GenericTypeReflector.getTypeParameter(view.getClass(), View.class.getTypeParameters()[0]);

        // Find Presenter instance
        String[] presenterBeanNames = beanFactory.getBeanNamesForType(ResolvableType.forType(presenterType));
        Assert.notEmpty(presenterBeanNames, String.format("'%s' presenter not found for '%s' view", presenterType, view.getClass()));
        Object presenterBean = beanFactory.getBean(presenterBeanNames[0]);

        // Check that view argument of Presenter interface compatible with provided view type
        Type actualPresenterType = GenericTypeReflector.getExactSuperType(presenterBean.getClass(), Presenter.class);
        Type presenterViewType = GenericTypeReflector.getTypeParameter(actualPresenterType, Presenter.class.getTypeParameters()[0]);
        if (presenterViewType == null) {
            // set Object if presenter has no restrictions for View
            presenterViewType = Object.class;
        }
        boolean isCompatibleView = GenericTypeReflector.isSuperType(presenterViewType, view.getClass());
        Assert.isTrue(isCompatibleView, String.format("'%s' presenter can't be used with '%s' view due to incompatible signature", presenterBean.getClass(), view.getClass()));

        // Return proxy for view
        Presenter<View<?>> presenter = (Presenter<View<?>>) presenterBean;
        return createProxy(view, presenter);
    }

    private Object createProxy(Object originalView, Presenter<View<?>> presenter) {
        boolean isComponent = GenericTypeReflector.isSuperType(Component.class, originalView.getClass());

        ProxyFactory proxyFactory = new ProxyFactory(originalView);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice((MethodInterceptor) (invocation) -> {
            String name = invocation.getMethod().getName();
            Object result = invocation.proceed();

            if (name.equals("getPresenter")) {
                return presenter;
            } else if (isComponent) {
                if (name.equals("onAttach")) {
                    if (invocation instanceof ProxyMethodInvocation) {
                        presenter.onAttach((View<?>) ((ProxyMethodInvocation) invocation).getProxy());
                    } else {
                        presenter.onAttach((View<?>) invocation.getThis());
                    }
                } else if (name.equals("onDetach")) {
                    presenter.onDetach();
                }
            }

            return result;
        });

        return proxyFactory.getProxy();
    }

}
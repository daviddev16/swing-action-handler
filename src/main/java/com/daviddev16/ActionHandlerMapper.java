package com.daviddev16;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class ActionHandlerMapper implements ActionListener {

	private Map<String, MethodWrapper> methodWrapperMap;
	
	public ActionHandlerMapper() {
		this.methodWrapperMap = new HashMap<String, MethodWrapper>();
	}
	
	public void configureContainer(Container container) {
		Objects.requireNonNull(container, "Container must not be null.");
		extractSubscribers(container);
		configureActionListenerToContainer(container);
	}
	
	public void extractSubscribers(Object instance) {
		Objects.requireNonNull(instance, "Instance must not be null.");
		final Class<?> instanceClassType = instance.getClass();
		for (Method method : instanceClassType.getDeclaredMethods())
			subscribeAsMethodWrapper(instance, method);
	}
	
	private void configureActionListenerToContainer(Container parentComponent) {
		if (parentComponent == null)
			return;
		if (parentComponent instanceof JMenu) {
			JMenu menu = (JMenu) parentComponent;
			for (int i = 0; i < menu.getItemCount(); i++)
				configureActionListenerToContainer(menu.getItem(i));
		}
		else if (parentComponent instanceof JMenuBar) {
			JMenuBar menuBar = (JMenuBar) parentComponent;
			for (Component subMenuComponent : menuBar.getComponents())
				if (subMenuComponent instanceof Container)
					configureActionListenerToContainer((Container)subMenuComponent);
		}
		else if (parentComponent instanceof AbstractButton) {
			((AbstractButton)parentComponent).addActionListener(this);
		} else {
			for (Component component : parentComponent.getComponents()) {
				if (component instanceof Container)
					configureActionListenerToContainer((Container)component);
			}
		}
	}

	private void subscribeAsMethodWrapper(Object instance, Method method) {
		final ActionSubscribe actionSubscribe = method.getDeclaredAnnotation(ActionSubscribe.class);
		if (actionSubscribe != null)
			methodWrapperMap.putIfAbsent(actionSubscribe.value(), 
					new MethodWrapper(method, instance));
	}
	
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		final String performedActionCommand = actionEvent.getActionCommand();
		if (performedActionCommand == null || performedActionCommand.isEmpty()) {
			return;
		}
		MethodWrapper methodWrapper = methodWrapperMap.get(performedActionCommand);
		if (methodWrapper == null)
			return;
		try {
			executeMethodWrapper(methodWrapper, actionEvent);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ActionFailedException(String.format("Failed to excute action:"
					+ " \"%s\".", performedActionCommand), e);
		}
	}
	
	private void executeMethodWrapper(MethodWrapper methodWrapper, ActionEvent actionEvent) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Method wrapperedMethod = methodWrapper.getMethod();
		final Object methodInstance = methodWrapper.getInstance();
		Object[] methodParameterValues = null;
		if (wrapperedMethod.getParameterCount() > 1)
			throw new IllegalStateException(String.format("\"%s\" has too "
					+ "many arguments.", wrapperedMethod.getName()));
		if (wrapperedMethod.getParameterCount() == 1) {
			Parameter uniqueParameter = wrapperedMethod.getParameters()[0];
			if (uniqueParameter.getType().isAssignableFrom(ActionEvent.class))
				methodParameterValues = new Object[] { actionEvent };
			else throw new IllegalStateException(String.format("\"%s\" is not a %s "
					+ "many arguments.", uniqueParameter.getName(), ActionEvent.class.getName()));
		} else {
			methodParameterValues = new Object[0];
		}
		wrapperedMethod.invoke(methodInstance, methodParameterValues);
	}
	
}

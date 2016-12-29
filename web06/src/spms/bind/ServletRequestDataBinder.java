package spms.bind;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletRequest;
/*
 * �ľǵ� dataName�� dataType�� �˸°� �����ϴ� .bind��� �޼��尡 �ִ�.
 * �� bind �޼��带 �� Ŭ�������� �����Ѵ�.
 * 1.�ʿ��� ������ Ÿ���� primitive Type�� �������� ��쿡�� �׿� �´� ��ü�� �ٷ� ����
 * 2.member��ü�� �����ϰ�, findsetter �޼��带 ����, ��������� �� �޼��带 ã�´�.
 * 3.method.invoke�� �̿��Ͽ�, ������ �����Ͽ� set �޼��带 ������� �ʿ��� data�� �غ��Ѵ�.
*/
public class ServletRequestDataBinder {
	//����Ʈ ��Ʈ�ѷ�(DispatcherServlet)������ ��û�� �´� Data�� �غ��ϱ� ���� �޼��带 �����Ѵ�.
	// ServletRequestDataBinder.bind(request, dataType, dataName); ��û�� �´� vo�� �����ϱ� ����
	//bind �޼��带 �����ϱ� ���� ServletRequestDataBinderŬ������ �����Ѵ�.
	public static Object bind(ServletRequest request, Class<?> dataType, String dataName) throws Exception {

		//1.dataType�� �� ��ü�� �����.
		//�⺻Ÿ��(String / data)�̸� if���ǹ��� true�� ������ �ǰ� �ٷ� ������ �Ѵ�.
		if (isPrimitiveType(dataType)) {
			return createValueObject(dataType, request.getParameter(dataName));
		}
		//jsp/html���� form ���ؼ�  �Է¹��� ������ request.getParameterMap()�ȿ� �����ϰ� �ǰ�, keyset()�޼��带 ���ؼ� map�ȿ� �ִ� ���� �����ü��� �ִ�.
		Set<String> paramNames = request.getParameterMap().keySet();
		//newInstance()�޼���� �⺻ �����ڸ� ȣ���ؼ� ��ü�� �����ϱ� ������ �ݵ�� Ŭ������ �⺻ �����ڰ� �����ؾ� �Ѵ�
		Object dataObject = dataType.newInstance();
		Method m = null;
		// (name, email, password) : keySet
		for (String paramName : paramNames) {// �迭�� ������ ����� �� ���� for������ �����ϰ� ���
			//������Ÿ�԰� �Ű��������� �ָ� set �޼��带 ã�Ƽ� ��ȯ�մϴ�. set�޼��带 ã������ ������ ������ dataObject�� ���� ȣ���� �Ѵ�.
			m = findSetter(dataType, paramName);
			//
			if (m != null) {
				//method Ŭ������ �޼���, �������� ȣ��� �޼����� ���(Objdect)����
				m.invoke(dataObject,
						createValueObject(m.getParameterTypes()[0],//���� �޼����� �Ű����� Ÿ��
						request.getParameter(paramName)));//��û �Ű������� ��
				
			}
		}
		return dataObject;
	}
	// .bind(request, dataName, dataType)�� ���ؼ� �ʿ��� DataName, DataType�� �м��� �Ѵ�.
	//�Ű������� �־��� Ÿ���� �⺻ Ÿ������ �˻��ϴ� �޼����̴�.
	//if���ǹ��� ����Ͽ� ������Ÿ���� int���� �ƴϸ� Integer Ŭ���� ���� �˻縦 �Ѵ�. 
	private static boolean isPrimitiveType(Class<?> type) {
		if (type.getName().equals("int") || type == Integer.class
			|| type.getName().equals("long") || type == Long.class
			|| type.getName().equals("float") || type == Float.class
			|| type.getName().equals("double") || type == Double.class 
			|| type.getName().equals("boolean") || type == Boolean.class
			|| type == Date.class || type == String.class) {
			return true;
		}
		return false;
	}
	//createValueObject �޼���� ���ͷ� ���� �Ҵ� �� �� ���� �⺻Ÿ�Կ� ���� ��ü�� �����ϴ� �޼����̴�.
	private static Object createValueObject(Class<?> type, String value) {
		if (type.getName().equals("int") || type == Integer.class) {
			return new Integer(value);
		} else if (type.getName().equals("float") || type == Float.class) {
			return new Float(value);
		} else if (type.getName().equals("double") || type == Double.class) {
			return new Double(value);
		} else if (type.getName().equals("long") || type == Long.class) {
			return new Long(value);
		} else if (type.getName().equals("boolean") || type == Boolean.class) {
			return new Boolean(value);
		} else if (type == Date.class) {
			return java.sql.Date.valueOf(value);
		} else {
			return value;
		}
	}
	//findSetter()�� Ŭ����(type)�� �����Ͽ� �־��� �̸�(name)�� ��ġ�ϴ� set�޼��带 ã���ϴ�.
	private static Method findSetter(Class<?> type, String name) {
		Method[] methods = type.getMethods();

		String propName = null;
		//�޼��� ����� �ݺ��ϸ鼭 set�޼��带�� ���ؼ��� �۾��� �����ϰ�, �޼��� ��Ͼȿ� set���� �������� �ʴ´ٸ� �����Ѵ�.
		for (Method m : methods) {
			if (!m.getName().startsWith("set"))
				continue;
			
			//set�޼����� ��� ��û �Ű������� �̸��� ��ġ�ϴ��� �˻��Ѵ�.
			//substring()�Լ��� set�޼��� �̸����� �ձ��ڸ� �����ϰ� ����Ѵ�.�� setxxxx���� set�� �����ϰ� xxxx�� ����Ѵ�.
			propName = m.getName().substring(3);		
			//propName�� �ִ� ���ڿ��߿� toLowerCase()�Լ��� ���ؼ� �빮�ڸ� �ҹ��ڷ� ��ȯ�Ѵ�.
			
			if (propName.toLowerCase().equals(name.toLowerCase())) {
				return m;
				//��ġ�ϴ� set�޼��带 ã�Ҵٸ� ��� ��ȯ�� �Ѵ�.
			}
		}
		return null;
	}
}
package spms.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import spms.bind.DataBinding;
import spms.bind.ServletRequestDataBinder;
import spms.controls.*;
import spms.vo.Member;

@SuppressWarnings("serial")
@WebServlet("*.do")
public class DispatcherServlet extends HttpServlet {
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		String servletPath = request.getServletPath();
		try {

			Controller controller = null;
			ServletContext sc = this.getServletContext();
			Map<String, Object> model = new HashMap<String, Object>();
			// model �ȿ� memberDao ����
			// ���� 1 : ��� ��Ʈ�ѷ��� dao�� ���Թް� �ȴ�.
			// model.put("memberDao", sc.getAttribute("memberDao"));
			// System.out.println(sc.getAttribute("memberDao")+" : sc üũ");

			// begin = ��û�б� & pageController ����
			model.put("session", request.getSession());
			controller = (Controller) sc.getAttribute(servletPath);

			// ������ ������ instanceof(��ü[����]+instanceof+Ÿ��[������] ��ġ)
			if (controller instanceof DataBinding) {
				// �𵨰�ü�� �ڵ����� ����� �ִ� �޼��带 ȣ���Ѵ�.
				// controller.getDataBinders();()ȣ�� �ʿ��� ��Ÿ���� Ǯ���.
				Object[] dataBinders = ((DataBinding) controller).getDataBinders();
				// ������ �� ��üŸ�� ��ü �̸�
				String dataName = null;
				Class<?> dataType = null;
				Object dataObj = null;
				for (int i = 0; i < dataBinders.length; i += 2) {
					dataName = (String) dataBinders[i];
					dataType = (Class<?>) dataBinders[i + 1];
					// some�ż��� ȣ�� -> dataType+request
					dataObj = ServletRequestDataBinder.bind(request, dataType, dataName);
					// name -> dataType.setName(name��);
					model.put(dataName, dataObj);
				}
			}

			// ��Ʈ�� ȣ���� ���� view �̸��� ���� �޽��ϴ�.
			System.out.println("controllerȮ�� : " + controller);
			String viewUrl = controller.execute(model);
			// �� �ȿ� �ִ� ����
			// map -> request.attribute�� �Űܰ���.
			for (String key : model.keySet()) {
				request.setAttribute(key, model.get(key));
			}
			if (viewUrl.startsWith("redirect:")) {
				response.sendRedirect(viewUrl.substring(9));
				return;

			} else {
				RequestDispatcher rd = request.getRequestDispatcher(viewUrl);
				rd.include(request, response);
			}

		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", e);
			RequestDispatcher rd = request.getRequestDispatcher("/Error.jsp");
			rd.forward(request, response);
		}
	}
}

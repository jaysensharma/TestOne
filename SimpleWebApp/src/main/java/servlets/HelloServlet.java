package servlets;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet {

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      Writer writer = resp.getWriter();
      writer.write("<html><body>");
      writer.write("<font color=green><h1>*Hello World !!!*</h1></font> (from HelloServlet)");
      writer.write("</body></html>");
   }
}

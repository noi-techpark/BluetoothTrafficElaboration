/*

BluetoothTrafficElaboration: various elaborations of traffic data

Copyright (C) 2017 IDM Südtirol - Alto Adige - Italy

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.idmsuedtirol.bluetoothtrafficelaboration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idmsuedtirol.bluetoothtrafficelaboration.ElaborationsInfo.TaskInfo;

/**
 * @author Davide Montesin <d@vide.bz>
 */
public class BluetoothTrafficElaborationServlet extends HttpServlet
{
   private static final String JDBC_CONNECTION_STRING = "JDBC_CONNECTION_STRING";

   String                      jdbcUrl;

   TaskThread                  taskThread;

   String                      schedulerTaskSql;

   private static String readResource(String name) throws IOException
   {
      InputStream in = BluetoothTrafficElaborationServlet.class.getResourceAsStream(name);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOUtils.copy(in, out);
      in.close();
      out.close();
      String result = new String(out.toByteArray(), "utf-8");
      return result;
   }

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      try
      {
         super.init(config);
         this.jdbcUrl = System.getProperty(JDBC_CONNECTION_STRING);
         // TODO as system parameter
         Class.forName("org.postgresql.Driver");
         this.schedulerTaskSql = readResource("scheduler_task.sql");

         this.taskThread = new TaskThread(this);
         this.taskThread.start();
      }
      catch (Exception exxx)
      {
         throw new ServletException(exxx);
      }

   }

   int executeDatabaseUpdate(String sql, Object[] args) throws SQLException
   {
      Connection conn = DriverManager.getConnection(this.jdbcUrl);
      try
      {
         conn.setAutoCommit(false);
         PreparedStatement ps = conn.prepareStatement(sql);
         for (int i = 0; i < args.length; i++)
         {
            ps.setObject(i + 1, args[i]);
         }
         int result = ps.executeUpdate();
         if (result < 1)
            throw new IllegalStateException();
         conn.commit();
         return result;
      }
      finally
      {
         conn.rollback();
         conn.close();
      }
   }

   ArrayList<TaskInfo> selectTaskInfo() throws SQLException
   {
      Connection conn = DriverManager.getConnection(this.jdbcUrl);
      try
      {
         ArrayList<TaskInfo> result = new ArrayList<TaskInfo>();

         ResultSet rs = conn.createStatement().executeQuery(this.schedulerTaskSql);
         while (rs.next())
         {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.id = rs.getLong("id");
            taskInfo.calc_order = rs.getInt("calc_order");
            taskInfo.function_name = rs.getString("function_name");
            taskInfo.args = rs.getString("args");
            taskInfo.enabled = rs.getString("enabled").equals("T");
            taskInfo.running = rs.getBoolean("running");
            taskInfo.last_start_time = rs.getString("last_start_time");
            taskInfo.last_duration = rs.getString("last_duration");
            taskInfo.last_status = rs.getString("last_status");
            taskInfo.same_status_since = rs.getString("same_status_since");
            taskInfo.last_run_output = rs.getString("last_run_output");
            result.add(taskInfo);
         }

         return result;
      }
      finally
      {
         conn.close();
      }

   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      try
      {
         ElaborationsInfo elaborationsInfo = new ElaborationsInfo();
         elaborationsInfo.taskThreadAlive = this.taskThread.isAlive();
         synchronized (this.taskThread.exclusiveLock)
         {
            elaborationsInfo.tashThreadRunning = !this.taskThread.sleeping;
         }

         elaborationsInfo.tasks.addAll(this.selectTaskInfo());

         ObjectMapper mapper = new ObjectMapper();
         mapper.setVisibility(mapper.getVisibilityChecker().withFieldVisibility(Visibility.NON_PRIVATE));
         StringWriter sw = new StringWriter();
         mapper.writeValue(sw, elaborationsInfo);
         resp.getWriter().write(sw.toString());
      }
      catch (Exception exxx)
      {
         throw new ServletException(exxx);
      }
   }

   @Override
   public void destroy()
   {
      super.destroy();
      this.taskThread.interrupt();
      try
      {
         this.taskThread.join();
      }
      catch (InterruptedException e)
      {
         // TODO should never happens: notify crashbox or throw a RuntimeException
         e.printStackTrace();
      }
   }
}

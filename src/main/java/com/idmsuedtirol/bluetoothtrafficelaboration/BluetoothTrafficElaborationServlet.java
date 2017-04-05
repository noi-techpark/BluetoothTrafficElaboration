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

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      try
      {
         super.init(config);
         this.jdbcUrl = System.getProperty(JDBC_CONNECTION_STRING);
         // TODO as system parameter
         Class.forName("org.postgresql.Driver");
         this.schedulerTaskSql = new Scanner(this.getClass().getResourceAsStream("scheduler_task.sql"))
                                                                                                       .useDelimiter("\\z")
                                                                                                       .next();
         this.taskThread = new TaskThread(this);
         this.taskThread.start();
      }
      catch (Exception exxx)
      {
         throw new ServletException(exxx);
      }

   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      try
      {
         ElaborationsInfo elaborationsInfo = new ElaborationsInfo();
         elaborationsInfo.taskThreadAlive = this.taskThread.isAlive();

         Connection conn = DriverManager.getConnection(this.jdbcUrl);
         try
         {
            // conn.setAutoCommit(false);
            ResultSet rs = conn.createStatement().executeQuery(this.schedulerTaskSql);
            while (rs.next())
            {
               TaskInfo taskInfo = new TaskInfo();
               taskInfo.calc_order = rs.getInt("calc_order");
               taskInfo.function_name = rs.getString("function_name");
               taskInfo.args = rs.getString("args");
               taskInfo.enabled = rs.getString("enabled").equals("T");
               taskInfo.last_run_time = rs.getString("last_run_time");
               taskInfo.status = rs.getString("status");
               elaborationsInfo.tasks.add(taskInfo);
            }
         }
         finally
         {
            conn.close();
         }

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
         // TODO notify crashbox or throw a RuntimeException
         e.printStackTrace();
      }
   }
}

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

/**
 * @author Davide Montesin <d@vide.bz>
 */
public class TaskThread extends Thread
{
   BluetoothTrafficElaborationServlet elaborationServlet;

   boolean                            stop = false;

   public TaskThread(BluetoothTrafficElaborationServlet elaborationServlet)
   {
      this.elaborationServlet = elaborationServlet;
   }

   @Override
   public void run()
   {
      while (true)
      {
         // TODO execute elaborations
         try
         {
            // Wait some time before repeat the elaborations
            Thread.sleep(15L * 60L * 1000L);
         }
         catch (InterruptedException e)
         {
            if (this.stop)
            {
               return;
            }
            throw new IllegalStateException("InterruptedException without stop");
         }
      }
   }

   @Override
   public void interrupt()
   {
      this.stop = true;
      super.interrupt();
   }

}

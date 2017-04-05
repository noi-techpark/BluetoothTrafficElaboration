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
/*
 Author: Davide Montesin <d@vide.bz>
 */
(function()
{
   window.com_idmsuedtirol_bte = function()
   {
      
      request_data();

      function request_data()
      {
         var xhttp = new XMLHttpRequest();
         xhttp.onreadystatechange = function()
         {
            if (this.readyState == 4)
            {
               if (this.status == 200)
               {
                  var data = JSON.parse(this.responseText);
                  refresh_ui(data)
               }
               else
               {
                  alert('Error: ' + this.status)
               }
            }
         };
         xhttp.open('GET', 'data', true);
         xhttp.send();
      }

      function refresh_ui(data)
      {
         var tasktable = document.getElementById('task');
         document.getElementById('sched_live').innerText = data.taskThreadAlive;
         var taskRow = document.getElementById('task-row-template');
         taskRow.parentElement.removeChild(taskRow);
         var tasks = data.tasks;
         for (var i = 0; i < tasks.length; i++)
         {
            var taskTr = taskRow.cloneNode(true);
            var tds = taskTr.getElementsByTagName('td');
            var task = tasks[i];
            tds[0].innerText = task.calc_order
            tds[1].innerText = task.function_name
            tds[2].innerText = task.args
            tds[3].innerText = task.enabled
            tds[4].innerText = task.last_run_time
            tds[5].innerText = task.status
            tds[5].className = task.status
            tasktable.appendChild(taskTr)
         }

      }
   }
})();

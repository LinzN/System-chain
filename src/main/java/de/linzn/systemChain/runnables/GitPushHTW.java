/*
 * Copyright (C) 2018. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 */

package de.linzn.systemChain.runnables;


import de.linzn.evy.EvyApp;
import de.linzn.evy.api.OperationRegister;
import de.linzn.evy.internal.containers.*;
import de.linzn.evy.module.notification.NotificationContainer;
import de.linzn.evy.module.notification.NotificationPriority;
import de.linzn.evy.plugin.runnables.data.Runner;
import org.json.JSONObject;

public class GitPushHTW extends Runner {

    @Override
    public void schedule() {
        DataContainer dataContainer = EvyApp.getInstance().getDatabaseModule().getData("shell_command_push_htw");
        String command = dataContainer.getJSON().getString("htw_command");
        String username = dataContainer.getJSON().getString("htw_userName");
        String hostname = dataContainer.getJSON().getString("htw_hostName");
        int port = dataContainer.getJSON().getInt("htw_port");
        TaskOperation taskOperation = OperationRegister.getOperation("run_linux_shell");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("useOutput", false);
        JSONObject sshObject = new JSONObject();
        jsonObject.put("ssh", sshObject);
        sshObject.put("useSSH", true);
        sshObject.put("user", username);
        sshObject.put("host", hostname);
        sshObject.put("port", port);
        JSONObject commandObject = new JSONObject();
        jsonObject.put("command", commandObject);
        commandObject.put("isScript", false);
        commandObject.put("script", command);

        TaskContainer taskContainer = new TaskContainer(taskOperation, jsonObject);
        addOperation(taskContainer);
    }

    @Override
    public void loopback(JSONObject jsonObject) {
        int exitCode = jsonObject.getInt("exitcode");
        if (exitCode != 0) {
            String message = "Unerwarteter Fehler bei Task [HTW Push] mit code " + exitCode;
            NotificationContainer notificationContainer = new NotificationContainer(message, NotificationPriority.HIGH);
            EvyApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public TimeData runnableTimer() {
        return new TimedTimeData(0, 1, 0);
    }

}

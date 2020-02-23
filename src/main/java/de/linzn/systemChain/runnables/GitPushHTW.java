/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.systemChain.runnables;


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.databaseModule.DataContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import org.json.JSONObject;

public class GitPushHTW extends AbstractCallback {

    @Override
    public void operation() {
        DataContainer dataContainer = AZCoreRuntimeApp.getInstance().getDatabaseModule().getData("shell_command_push_htw");
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
        addOperationData(taskOperation, jsonObject);
    }

    @Override
    public void callback(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        int exitCode = jsonObject.getInt("exitcode");
        if (exitCode != 0) {
            String message = "Unerwarteter Fehler bei Task [HTW Push] mit code " + exitCode;
            NotificationContainer notificationContainer = new NotificationContainer(message, NotificationPriority.HIGH);
            AZCoreRuntimeApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 1, 0, true);
    }


}

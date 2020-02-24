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

package de.linzn.systemChain.callbacks;


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationSettings;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;

import java.io.File;

public class GitPushHTW extends AbstractCallback {

    private FileConfiguration fileConfiguration;

    public GitPushHTW() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "gitPushHTW.yml"));
        fileConfiguration.get("hostname", "test");
        fileConfiguration.get("username", "test");
        fileConfiguration.get("port", 22);
        fileConfiguration.get("command", "ssh test");
        fileConfiguration.save();
    }

    @Override
    public void operation() {
        String command = fileConfiguration.getString("command");
        String username = fileConfiguration.getString("username");
        String hostname = fileConfiguration.getString("hostname");
        int port = fileConfiguration.getInt("port");

        TaskOperation taskOperation = OperationRegister.getOperation("run_linux_shell");
        OperationSettings operationSettings = new OperationSettings();

        operationSettings.addSetting("ssh.use", true);
        operationSettings.addSetting("ssh.user", username);
        operationSettings.addSetting("ssh.host", hostname);
        operationSettings.addSetting("ssh.port", port);

        operationSettings.addSetting("command.script", command);

        operationSettings.addSetting("output.use", false);
        addOperationData(taskOperation, operationSettings);
    }

    @Override
    public void callback(Object object) {
        OperationSettings operationSettings = (OperationSettings) object;

        int exitCode = operationSettings.getIntSetting("exit");
        if (exitCode != 0) {
            String message = "Git push to HTW error with code " + exitCode;
            NotificationContainer notificationContainer = new NotificationContainer(message, NotificationPriority.HIGH);
            AZCoreRuntimeApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 1, 30, true);
    }


}

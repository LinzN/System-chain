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


import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.notificationModule.NotificationContainer;
import de.stem.stemSystem.modules.notificationModule.NotificationPriority;
import de.stem.stemSystem.taskManagment.AbstractCallback;
import de.stem.stemSystem.taskManagment.CallbackTime;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;
import de.stem.stemSystem.taskManagment.operations.defaultOperations.ShellOperation;

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

        ShellOperation shellOperation = new ShellOperation();

        shellOperation.setUseSSH(true);
        shellOperation.setSshUser(username);
        shellOperation.setSshHost(hostname);
        shellOperation.setSshPort(port);

        shellOperation.setScriptCommand(command);
        shellOperation.setUseOutput(false);

        addOperationData(shellOperation);
    }

    @Override
    public void callback(OperationOutput operationOutput) {

        int exitCode = operationOutput.getExit();
        if (exitCode != 0) {
            String message = "Git push to HTW error with code " + exitCode;
            NotificationContainer notificationContainer = new NotificationContainer(message, NotificationPriority.HIGH);
            STEMSystemApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 1, 50, true);
    }


}

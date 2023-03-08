package com.tabnine;

import static com.tabnine.general.DependencyContainer.*;

import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.config.Config;
import com.tabnine.general.StaticConfig;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.BinaryStateService;
import com.tabnine.lifecycle.TabnineUpdater;
import com.tabnine.logging.LogInitializerKt;
import com.tabnine.notifications.ConnectionLostNotificationHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class Initializer extends PreloadingActivity implements StartupActivity {
  private BinaryNotificationsLifecycle binaryNotificationsLifecycle;
  private BinaryPromotionStatusBarLifecycle binaryPromotionStatusBarLifecycle;
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private static final ConnectionLostNotificationHandler connectionLostNotificationHandler =
      new ConnectionLostNotificationHandler();

  @Override
  public void preload(@NotNull ProgressIndicator indicator) {
    initialize();
  }

  @Override
  public void runActivity(@NotNull Project project) {
    initialize();
  }

  private void initialize() {

    boolean shouldInitialize =
        !(initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode());
    if (shouldInitialize) {
      Logger.getInstance(getClass())
          .info(
              "Initializing for "
                  + Config.CHANNEL
                  + " onprem="
                  + Config.IS_ON_PREM
                  + ", plugin id = "
                  + StaticConfig.TABNINE_PLUGIN_ID_RAW);

      connectionLostNotificationHandler.startConnectionLostListener();
      ServiceManager.getService(BinaryStateService.class).startUpdateLoop();

      if (!Config.IS_ON_PREM) {
        LogInitializerKt.init();
        binaryNotificationsLifecycle = instanceOfBinaryNotifications();
        binaryPromotionStatusBarLifecycle = instanceOfBinaryPromotionStatusBar();
        binaryNotificationsLifecycle.poll();
        binaryPromotionStatusBarLifecycle.poll();
        CapabilitiesService.getInstance().init();
        TabnineUpdater.pollUpdates();
        PluginInstaller.addStateListener(instanceOfUninstallListener());
      }
    }
  }
}

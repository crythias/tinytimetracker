// SPDX-License-Identifier: GPL-2.0-only
package tracker;

/**
 * AutoStart Interface for version 1 implementations.
 * Provides methods to enable or disable auto-start functionality
 * and to retrieve the associated JCheckBoxMenuItem.
 * Because of conflict reasons, this interface is named AutoStart_1.
 */
import javax.swing.JCheckBoxMenuItem;

public interface AutoStart_1 {
    public void autoStart(boolean autoStart);
    public JCheckBoxMenuItem getAutoStartCheckBox();
}

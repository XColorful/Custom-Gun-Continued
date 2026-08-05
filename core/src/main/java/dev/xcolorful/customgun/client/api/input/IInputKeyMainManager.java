/*
 * Go to BattleRoyale GameManager 的设计
 */

package dev.xcolorful.customgun.client.api.input;

import java.util.List;

public interface IInputKeyMainManager extends IInputKeySubManager {

    boolean registerSubManager(IInputKeySubManager subManager);
    boolean unregisterSubManager(IInputKeySubManager subManager);

    List<IInputKeySubManager> getSubManagers();
}

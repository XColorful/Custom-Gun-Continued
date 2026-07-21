/*
 * Go to BattleRoyale GameManager 的设计
 */

package xiao.customgun.client.api.input;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.input.InputKey;

/**
 * 设计用途:
 * <ul>
 *     <li>集中监听键鼠事件并过滤</li>
 *     <li>避免{@link InputKey}需要同时检查键鼠输入</li>
 *     <li>不限制子类额外监听事件</li>
 * </ul>
 */
public interface IInputKeyManager extends IInputKeyMainManager {

    @Override
    default @Nullable IKeyMapping getKeyMapping() {
        return null;
    }
}

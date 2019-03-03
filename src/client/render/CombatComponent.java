package client.render;

import com.almasb.fxgl.entity.component.Component;
import shared.CombatObject;



public class CombatComponent extends Component {
    private CombatObject combatObject;

    public CombatComponent(CombatObject object) {
        combatObject = object;
    }

    public CombatObject getCombatObject() {
        return combatObject;
    }

    public void setCombatObject(CombatObject combatObject) {
        this.combatObject = combatObject;
    }
}

package me.kwilson272.collisionutility;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.TorrentWave;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;

import com.projectkorra.projectkorra.waterbending.ice.IceSpikePillar;
import me.kwilson272.collisionutility.parser.Lexer;
import me.kwilson272.collisionutility.parser.Parser;
import me.kwilson272.collisionutility.parser.grammar.CollisionDeclaration;
import me.kwilson272.collisionutility.parser.grammar.Expression;
import me.kwilson272.collisionutility.parser.grammar.GroupDeclaration;
import me.kwilson272.collisionutility.parser.grammar.Operand;
import me.kwilson272.collisionutility.parser.token.Token;
import me.kwilson272.collisionutility.parser.token.TokenType;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Handles converting parsed expressions into collisions and registering them.
 */
public class CollisionLoader {

    private boolean doVerbose;

    private final Plugin plugin;
    private final Map<String, List<CoreAbility>> groups;
    private final Map<String, CoreAbility> abilityConversions;

    public CollisionLoader(Plugin plugin) {
        this.plugin = plugin;
        groups = new HashMap<>();

        // Some Abilities in PK share names, which makes it impossible for us to retrieve
        // the CoreAbility we want, so we will provide some alternate ways of referring
        // to these classes
        abilityConversions = new HashMap<>();
        abilityConversions.put("FireBlast", CoreAbility.getAbility(FireBlast.class));
        abilityConversions.put("FireBlastCharged", CoreAbility.getAbility(FireBlastCharged.class));
        abilityConversions.put("ChargedFireBlast", CoreAbility.getAbility(FireBlastCharged.class));
        abilityConversions.put("CFB", CoreAbility.getAbility(FireBlastCharged.class));
        abilityConversions.put("IceSpikeBlast", CoreAbility.getAbility(IceSpikeBlast.class));
        abilityConversions.put("IceSpike", CoreAbility.getAbility(IceSpikePillar.class));
        abilityConversions.put("WaterSpout", CoreAbility.getAbility(WaterSpout.class));
        abilityConversions.put("WaterWave", CoreAbility.getAbility(WaterSpoutWave.class));
        abilityConversions.put("WaterSpoutWave", CoreAbility.getAbility(WaterSpoutWave.class));
        abilityConversions.put("Torrent", CoreAbility.getAbility(Torrent.class));
        abilityConversions.put("TorrentWave", CoreAbility.getAbility(TorrentWave.class));
        abilityConversions.put("TorrentRing", CoreAbility.getAbility(TorrentWave.class));
        abilityConversions.put("Surge", CoreAbility.getAbility(SurgeWave.class));
        abilityConversions.put("SurgeWave", CoreAbility.getAbility(SurgeWave.class));
        abilityConversions.put("SurgeWall", CoreAbility.getAbility(SurgeWall.class));
        abilityConversions.put("SurgeShield", CoreAbility.getAbility(SurgeWall.class));
    }

    /**
     * Parses the collisions file and loads them into ProjectKorra
     */
    public void loadCollisions() {
        FileConfiguration config = plugin.getConfig();
        doVerbose = config.getBoolean("Properties.Verbose", false);
        if (config.getBoolean("Properties.DisableCoreCollisions", false)) {
            plugin.getLogger().log(Level.INFO, "Clearing out core collisions... ");
            ProjectKorra.getCollisionManager().getCollisions().clear();
        }

        plugin.getLogger().log(Level.INFO, "Initializing Collisions...");

        String fileName = config.getString("Properties.CollisionFile", "collisions.txt");
        File file = new File(plugin.getDataFolder() + File.separator + fileName);
        List<Token> tokens = new Lexer(file, plugin.getLogger()).readTokens();
        List<Expression> expressions = new Parser(tokens, plugin.getLogger()).parseExpressions();

        for (Expression expression : expressions) {
            if (expression instanceof GroupDeclaration groupDeclaration) {
                loadGroup(groupDeclaration);
            } else if (expression instanceof CollisionDeclaration collisionDeclaration) {
                loadCollision(collisionDeclaration);
            }
        }
        plugin.getLogger().log(Level.INFO, "Finished Initializing Collisions");
    }

    /**
     * Loads an ability grouping from a GroupDeclaration object
     * @param groupDeclaration the object from which to load
     */
    private void loadGroup(GroupDeclaration groupDeclaration) {
        if (groupDeclaration.groupName().isEmpty()) {
            groups.put(groupDeclaration.groupName(), Collections.emptyList());
            return;
        }

        List<CoreAbility> abilities = groupDeclaration.operands().stream()
                .flatMap(operand -> getAbilities(operand).stream())
                .collect(Collectors.toList());
        groups.put(groupDeclaration.groupName(), abilities);
    }

    /**
     * Gets all abilities from an operand, regardless of if it is a group
     * reference or a single ability name.
     *<p>
     * It is important to note that this operates on a single-pass system,
     * meaning that groups cannot be referenced on a line before they are
     * declared.
     *<p>
     * We treat even single abilities as a list for simplicity. Performance
     * is not super important given the time the plugin runs
     *
     * @param operand the Operand from which the Abilities are derived
     * @return a List of CoreAbilities
     */
    private List<CoreAbility> getAbilities(Operand operand) {
        if (!operand.isGroupReference()) {
            CoreAbility ability = stringToAbility(operand.literal());
            if (ability == null) {
                plugin.getLogger().warning("Could not find ability with the name: "
                        + operand.literal() + " on line: " + operand.lineNumber());
                return Collections.emptyList();
            }
            return Collections.singletonList(ability);
        }

        if (!groups.containsKey(operand.literal())) {
            plugin.getLogger().warning("Could not find a group with the name: "
                    + operand.literal() + " on line: " + operand.lineNumber());
            return Collections.emptyList();
        }
        return groups.get(operand.literal());
    }

    /**
     * Converts a case-sensitive string to a CoreAbility. This method will return null
     * if an Ability could not be found.
     *
     * @param abilityName the String ability name
     * @return a CoreAbility, or null if one couldn't be found
     */
    private CoreAbility stringToAbility(String abilityName) {
        return abilityConversions.getOrDefault(abilityName, CoreAbility.getAbility(abilityName));
    }

    private void loadCollision(CollisionDeclaration collisionDeclaration) {
        List<CoreAbility> abilitiesFirst = getAbilities(collisionDeclaration.leftOperand());
        TokenType operator = collisionDeclaration.operator().type();
        List<CoreAbility> abilitiesSecond = getAbilities(collisionDeclaration.rightOperand());

        for (CoreAbility first : abilitiesFirst) {
            for (CoreAbility second : abilitiesSecond) {
                setUpCollision(first, operator, second);
            }
        }
    }

    /**
     * Sets up a collision between the two provided CoreAbilities, and registers it
     * with ProjectKorra.
     *
     * @param first the Ability on the left-hand side of the expression
     * @param operator the Operator dictating which abilities are removed
     * @param second the Ability on the right-hand side of the expression
     */
    private void setUpCollision(CoreAbility first, TokenType operator, CoreAbility second) {
       if (operator == TokenType.X) {
           removeCollision(first, second);
           if (doVerbose) {
               plugin.getLogger().log(Level.INFO, "Removing collision between: "
                       + first.getName() + " and " + second.getName());
           }
           return;
       }

       // Default operator in this case is ~
       boolean removeFirst = switch (operator) {
           case LESS_THAN, EQUAL -> true;
           default -> false;
       };
       boolean removeSecond = switch (operator) {
           case GREATER_THAN, EQUAL -> true;
           default -> false;
       };

       Collision collision = new Collision(first, second, removeFirst, removeSecond);
       ProjectKorra.getCollisionManager().addCollision(collision);
       if (doVerbose) {
           plugin.getLogger().log(Level.INFO, "Registering Collision between: "
                   + first.getName() + " and " + second.getName() + " RemoveFirst: "
                   + removeFirst + " RemoveSecond: " + removeSecond);
       }
    }

    /**
     * Removes a collision from the collision manager where the ability pair matches
     * the provided abilities. This method does not respect the order of the collision.
     *
     * @param first  a CoreAbility that makes up part of the Collision pair
     * @param second a CoreAbility that makes up part of the Collision pair
     */
    private void removeCollision(CoreAbility first, CoreAbility second) {
        // just setting removeFirst and removeSecond doesn't seem to work, so we have
        // to do this weird iteration
        ProjectKorra.getCollisionManager().getCollisions().removeIf(collision -> {
            CoreAbility collisionFirst = collision.getAbilityFirst();
            CoreAbility collisionSecond = collision.getAbilitySecond();
            return ((collisionFirst.equals(first) && collisionSecond.equals(second))
                    || (collisionSecond.equals(first) && collisionFirst.equals(second)));
        });
    }
}

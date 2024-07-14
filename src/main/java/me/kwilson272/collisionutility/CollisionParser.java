package me.kwilson272.collisionutility;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CollisionParser {

    private static final char[] OPERATORS = {'>', '<', '=', 'x', '~'};

    public enum ParseResult {

        SUCCESSFUL_PARSE(""),

        GROUP_MISSING_NAME("Invalid group declaration: missing name"),
        GROUP_MISSING_QUOTATION("Invalid group declaration: missing end quotation"),
        GROUP_EMPTY_NAME("Invalid group declaration: attempt to declare a nameless group"),
        GROUP_MISSING_OPEN_BRACKET("Invalid group declaration: missing opening bracket"),
        GROUP_MISSING_CLOSE_BRACKET("Invalid group declaration: missing closing bracket"),

        COLLISION_MALFORMED("Malformed collision declaration, unable to read formatting"),
        COLLISION_MISSING_OPERATOR("Malformed collision declaration, missing operator"),
        COLLISION_MISSING_OPERAND1("Malformed collision declaration, missing first operand"),
        COLLISION_MISSING_OPERAND2("Malformed collision declaration, missing second operand");

        private final String errorMessage;

        ParseResult(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    private final CollisionUtility plugin;
    private final boolean verbose;

    public CollisionParser(CollisionUtility plugin) {
        this.plugin = plugin;

        plugin.getLogger().log(Level.INFO, "Initializing Collisions...");

        plugin.getDataFolder().mkdir();
        FileConfiguration config = plugin.getConfig();
        verbose = config.getBoolean("Properties.Verbose", true);

        if (config.getBoolean("Properties.DisableCoreCollisions", false)) {
            ProjectKorra.getCollisionManager().getCollisions().clear();
        }

        String fileName = config.getString("Properties.CollisionFile", "collisions.txt");
        File file = new File(plugin.getDataFolder() + File.separator + fileName);
        if (file.exists()) {
            int bufferSize = plugin.getConfig().getInt("Properties.BufferSize", 8000);
            loadCollisions(file, bufferSize);
        }
    }

    private void loadCollisions(File collisionFile, int bufferSize) {
        Map<String, List<String>> groups = new HashMap<>();
        try (BufferedReader fileReader =
                     new BufferedReader(new FileReader(collisionFile), bufferSize)) {

            int lineNumber = 0;
            String line;
            while ((line = fileReader.readLine()) != null) {
                ++lineNumber;

                int firstNonWhitespace = getFirstNonWhitespace(line);
                if (firstNonWhitespace == -1 || line.charAt(0) == '#') { // Empty line or comment
                    continue;
                }

                ParseResult parseResult;
                line = line.substring(firstNonWhitespace);
                if (line.startsWith("Group")) {
                    parseResult = parseGroup(line, groups);
                } else {
                    String[] operands = new String[3]; // Operand1, Operator, Operand2
                    parseResult = parseCollision(line, operands);
                    if (parseResult == ParseResult.SUCCESSFUL_PARSE) {
                        setupCollision(operands[1], operands[0], operands[2], groups);
                    }
                }

                if (parseResult != ParseResult.SUCCESSFUL_PARSE) {
                    plugin.getLogger().log(Level.WARNING, parseResult.errorMessage + " on line: " + lineNumber);
                }
            }
            plugin.getLogger().log(Level.INFO, "Finished reading all collisions.");
        } catch (FileNotFoundException e) { // Shouldn't happen
            plugin.getLogger().log(Level.INFO, "Unable to open the file: " + collisionFile.getName());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the index of the first non-whitespace character.
     * In the case that the String is entirely whitespace, this
     * method will return -1
     *
     * @param string the String being checked
     * @return the integer index of the first non-whitespace character
     */
    private int getFirstNonWhitespace(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the first whitespace character. In the
     * case that the string has no whitespace, this method will return -1
     *
     * @param string the string being checked
     * @return the integer index of the first non-whitespace character
     */
    private int getFirstWhitespace(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (Character.isWhitespace(string.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index the operator is at. This method will return -1 if
     * no operator can be found.
     *
     * @param string the String being checked
     * @return the integer index of the operator
     */
    private int getOperatorIndex(String string) {
        for (Character operator : OPERATORS) {
            int opIndex = string.indexOf(operator);
            if (opIndex != -1) {
                return opIndex;
            }
        }
        return -1;
    }

    private ParseResult parseGroup(String line, Map<String, List<String>> groups) {
        int beginQuote = line.indexOf("\"");
        int endQuote = line.lastIndexOf('\"');

        if (beginQuote == -1) {
            return ParseResult.GROUP_MISSING_NAME;
        } else if (beginQuote == endQuote) {
            return ParseResult.GROUP_MISSING_QUOTATION;
        } else if (beginQuote == endQuote-1) {
            return ParseResult.GROUP_EMPTY_NAME;
        }

        // +/- 1 to get rid of quotations
        String groupName =  line.substring(beginQuote+1, endQuote);

        int openingBracket = line.indexOf('{');
        int closingBracket = line.indexOf('}');
        if (openingBracket == -1) {
            return ParseResult.GROUP_MISSING_OPEN_BRACKET;
        } else if (closingBracket == -1) {
            return ParseResult.GROUP_MISSING_CLOSE_BRACKET;
        }

        String group = line.substring(openingBracket, closingBracket+1);
        List<String> abilityNames = new ArrayList<>();
        int end;
        while ((end = group.indexOf(',')) != -1 || (end = group.indexOf('}')) != -1) {
            if (end != 1) { // In case of formatting {, Ability, Ability}
                String ability = group.substring(1, end);
                abilityNames.add(ability);
                group = group.substring(end+1);
            }
        }

        if (verbose) {
            plugin.getLogger().log(Level.INFO, "Loaded a group with name: \"" + groupName + '\"');
        }

        groups.put(groupName, abilityNames);
        return ParseResult.SUCCESSFUL_PARSE;
    }

    private ParseResult parseCollision(String line, String[] operands) {
        int opIndex = getOperatorIndex(line);
        if (opIndex == -1) {
            return ParseResult.COLLISION_MISSING_OPERATOR;
        } else if (opIndex == line.length()-1) {
            return ParseResult.COLLISION_MISSING_OPERAND2;
        } else if (opIndex == 0) {
            return ParseResult.COLLISION_MISSING_OPERAND1;
        }
        operands[1] = String.valueOf(line.charAt(opIndex));

        int endIndex = getFirstWhitespace(line);
        if (endIndex == -1 || endIndex == 0) {
            return ParseResult.COLLISION_MALFORMED;
        }

        String operand1 = line.substring(0, endIndex);
        operands[0] = operand1;

        String lastOperand = line.substring(opIndex+1);
        int beginIndex = getFirstNonWhitespace(lastOperand);
        if (beginIndex == -1) {
            return ParseResult.COLLISION_MISSING_OPERAND2;
        }

        lastOperand = lastOperand.substring(beginIndex);
        endIndex = getFirstWhitespace(lastOperand);
        if (endIndex == -1) {
            endIndex = lastOperand.length();
        }

        String operand2 = lastOperand.substring(0, endIndex);
        operands[2] = operand2;

        return ParseResult.SUCCESSFUL_PARSE;
    }

    private void setupCollision(String operator, String op1, String op2, Map<String, List<String>> groups) {
        if (op1.startsWith("$")) {
            String groupName = op1.substring(1);
            List<String> abilityNames = groups.get(groupName);
            if (abilityNames == null) {
                plugin.getLogger().log(Level.WARNING, "Cannot find a group with the name: " + groupName);
                return;
            }

            for (String ability : abilityNames) {
                setupCollision(operator, ability, op2, groups);
            }
            return;
        } else if (op2.startsWith("$")) {
            String groupName = op2.substring(1);
            List<String> abilityNames = groups.get(groupName);
            if (abilityNames == null) {
                plugin.getLogger().log(Level.WARNING, "Cannot find a group with the name: " + groupName);
                return;
            }

            for (String ability : abilityNames) {
                setupCollision(operator, op1, ability, groups);
            }
            return;
        }

        CoreAbility first = parseCoreAbility(op1);
        CoreAbility second = parseCoreAbility(op2);
        if (first == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot find an ability with the name:" +
                    " \"" + op1 + "\"");
            return;
        } else if (second == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot find an ability with the name: " +
                    "\"" + op2 + "\"");
            return;
        }

        if (operator.equals("x")) {
            if (removeCollision(first, second) && verbose) {
                plugin.getLogger().log(Level.INFO, "Removed collision between: " + op1 + " and " + op2);
            }
            return;
        }

        // Default operator is "~"
        boolean removeFirst = false;
        boolean removeSecond = false;
        switch (operator) {
            case ">" -> removeSecond = true;
            case "<" -> removeFirst = true;
            case "=" -> {
                removeFirst = true;
                removeSecond = true;
            }
        }
        Collision collision = new Collision(first, second, removeFirst, removeSecond);
        ProjectKorra.getCollisionManager().addCollision(collision);

        if (verbose) {
            plugin.getLogger().log(Level.INFO, "Created a collision with: " + op1 + operator + op2);
        }
    }

    private CoreAbility parseCoreAbility(String abilityName) {
        // Some of these need to be done manually, some abilities have duplicate getName() values
        switch (abilityName) {
            case "FireBlast" -> {
                return CoreAbility.getAbility(FireBlast.class);
            }
            case "FireBlastCharged", "ChargedFireBlast", "CFB" -> {
                return CoreAbility.getAbility(FireBlastCharged.class);
            }
            case "IceSpikeBlast" -> {
                return CoreAbility.getAbility(IceSpikeBlast.class);
            }
            case "WaterSpout" -> {
                return CoreAbility.getAbility(WaterSpout.class);
            }
            case "WaterWave", "WaterSpoutWave" -> {
                return CoreAbility.getAbility(WaterSpoutWave.class);
            }
            default -> {
                return CoreAbility.getAbility(abilityName);
            }
        }
    }

    /**
     * Removes a collision from the collision manager where the ability pair matches
     * the provided abilities. This method does not respect the order of the collision.
     *
     * @param first a CoreAbility that makes up part of the Collision pair
     * @param second a CoreAbility that makes up part of the Collision pair
     */
    private boolean removeCollision(CoreAbility first, CoreAbility second) {
        return ProjectKorra.getCollisionManager().getCollisions().removeIf(collision -> {
            CoreAbility collisionFirst = collision.getAbilityFirst();
            CoreAbility collisionSecond = collision.getAbilitySecond();
            return ((collisionFirst.equals(first) && collisionSecond.equals(second))
                    || (collisionSecond.equals(first) && collisionFirst.equals(second)));
        });
    }
}

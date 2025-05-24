package me.kwilson272.collisionutility.parser.grammar;

import java.util.List;

/**
 * Representation of the group declaration grammar non-terminal
 * ex: group "smallAbilities" {AirBlast, FireBlast}
 *
 * @param groupName the Name of the group in the declaration
 * @param operands a List of the Operands declared as part of the group
 */
public record GroupDeclaration(String groupName, List<Operand> operands) implements Expression {}

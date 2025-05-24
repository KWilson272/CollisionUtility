package me.kwilson272.collisionutility.parser.grammar;

/**
 * Class representation of the operand grammar non-terminal.
 *
 * @param isGroupReference true if the operand is a group reference,
 *                        false if it is a regular ability
 * @param literal the String literal underlying the operand
 * @param lineNumber the line number the literal was declared on
 */
public record Operand(boolean isGroupReference, String literal, int lineNumber) {}

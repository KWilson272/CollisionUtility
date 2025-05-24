package me.kwilson272.collisionutility.parser.grammar;

import me.kwilson272.collisionutility.parser.token.Token;

/**
 * Representation of the collision_declaration grammar non-terminal.
 * Ex: AirBlast > FireBlast
 *
 * @param leftOperand the Operand on the left of the operator
 * @param operator the Token containing the operator
 * @param rightOperand the Operand on the right of the operator
 */
public record CollisionDeclaration(Operand leftOperand, Token operator, Operand rightOperand) implements Expression  {}



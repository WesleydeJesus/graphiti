/*
 * Copyright (c) 2008-2010, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

grammar Cal;

options {
  output = AST;
  k = 1;
}

tokens {
  // network
  Attribute;
  Connector;
  EntityDecl;
  EntityExpr;
  EntityPar;
  Network;
  StructureStmt;
  VarDecl;

  // actor
  Actor;
  Dot;
  Empty;
  Name;
  Inputs;
  Outputs;
  PortDecl;
  QualifiedId;

  // types
  Parameter;
  Type;
  TypeAttr;
  ExprAttr;
  TypePar;

  // expressions
  BinOp;
  Boolean;
  Expression;
  Integer;
  List;
  Minus;
  Not;
  Real;
  String;
  UnOp;
  Var;
}

@lexer::header {
package net.sf.graphiti.grammar;
}

@parser::header {
package net.sf.graphiti.grammar;
}

///////////////////////////////////////////////////////////////////////////////
// NETWORK

network: NETWORK qualifiedId (LBRACKET typePars? RBRACKET)?
  LPAREN parameters? RPAREN
  portSignature COLON
  oneImport* varDeclSection?
  entitySection? structureSection?
  END EOF ->
    ^(Network qualifiedId parameters? portSignature
      varDeclSection? entitySection? structureSection?);

portSignature: inputPorts DOUBLE_EQUAL_ARROW outputPorts -> inputPorts outputPorts;

inputPorts: portDecls -> ^(Inputs portDecls) | -> ^(Inputs Empty);

outputPorts: portDecls -> ^(Outputs portDecls) | -> ^(Outputs Empty);

// var declarations
varDeclSection:	VAR varDecl+ -> varDecl+;

varDecl: MUTABLE? typeAndId
  ((EQ | COLON_EQUAL) expression -> ^(VarDecl typeAndId ^(Expression expression))
  | -> ^(VarDecl typeAndId)) SEMICOLON;

// entities
entitySection: ENTITIES entityDecl+ -> entityDecl+;

entityDecl: ID EQ entityExpr SEMICOLON -> ^(EntityDecl ^(Var ID) entityExpr);

entityExpr: ID LPAREN entityPars? RPAREN -> ^(EntityExpr ^(Var ID) entityPars?);

entityPars: entityPar (COMMA entityPar)* -> entityPar+;

entityPar: ID EQ expression -> ^(EntityPar ^(Var ID) ^(Expression expression));

// structure section
structureSection: STRUCTURE structureStmt+ -> structureStmt+;

structureStmt: c1=connector DOUBLE_DASH_ARROW c2=connector at=attributeSection? SEMICOLON ->
  ^(StructureStmt $c1 $c2 $at?) ;

connector: v1=ID (
  DOT v2=ID -> ^(Connector ^(Var $v1) ^(Var $v2))
  | -> ^(Connector ^(Var $v1)));

attributeSection: LBRACE attributeDecl* RBRACE -> attributeDecl*;

attributeDecl: id=ID (EQ expression SEMICOLON -> ^(Attribute ^(Var $id) ^(Expression expression))
| COLON type SEMICOLON -> ^(Attribute ^(Var $id) ^(Type type))) ;

///////////////////////////////////////////////////////////////////////////////
// ACTOR

actor: oneImport* ACTOR ID
  LPAREN parameters? RPAREN
  portSignature COLON .* EOF ->
    ^(Actor ^(Name ID) parameters? portSignature);
    
qualifiedId: ID (DOT ID)* -> ^(QualifiedId ^(Var ID) (^(Dot DOT) ^(Var ID))*);

///////////////////////////////////////////////////////////////////////////////
// IMPORTS

oneImport: IMPORT importId SEMICOLON ;

importId: ID importIdRest?;

importIdRest: DOT (TIMES | ID importIdRest?);

///////////////////////////////////////////////////////////////////////////////
// PARAMETERS

parameter: typeAndId
  (EQ expression -> ^(Parameter typeAndId ^(Expression expression))
  | -> ^(Parameter typeAndId));

parameters: parameter (COMMA parameter)* -> parameter+;

///////////////////////////////////////////////////////////////////////////////
// PORT DECLARATIONS

portDecl: MULTI? typeAndId -> ^(PortDecl typeAndId);

portDecls: portDecl (COMMA portDecl)* -> portDecl+ ;

///////////////////////////////////////////////////////////////////////////////
// TYPES

mainParameter: typeAndId EOF -> ^(Parameter typeAndId);

typeAndId: typeName=ID
  (typeRest? varName=ID -> ^(Type ^(Var $typeName) typeRest?) ^(Var $varName)
  | -> ^(Var $typeName));

type: ID typeRest? -> ^(Type ^(Var ID) typeRest?);

typeRest: LBRACKET typePars? RBRACKET -> typePars?
  | LPAREN typeAttrs? RPAREN -> typeAttrs?;

typeAttrs: typeAttr (COMMA typeAttr)* -> typeAttr+;

typeAttr: ID typeAttrRest -> typeAttrRest;

typeAttrRest: COLON type -> ^(TypeAttr type)
| EQ expression -> ^(ExprAttr ^(Expression expression));

typePars: typePar (COMMA typePar)* -> typePar+;

typePar: ID (LT type)? -> ^(TypePar ID type?);

///////////////////////////////////////////////////////////////////////////////
// EXPRESSIONS

mainExpression: expression EOF -> ^(Expression expression);

expression: factor (binop factor)*;

unop: (op=MINUS | op=NOT) -> ^(UnOp $op);

binop: (op=PLUS | op=MINUS | op=TIMES | op=DIV | op=XOR) -> ^(BinOp $op);

factor: term
| unop term -> ^(Expression unop term);

term: atom
  | LPAREN expression RPAREN -> ^(Expression expression);

atom: ID -> ^(Var ID)
| FLOAT -> ^(Real FLOAT)
| INTEGER -> ^(Integer INTEGER)
| STRING -> ^(String STRING)
| TRUE -> ^(Boolean TRUE)
| FALSE -> ^(Boolean FALSE)
| LBRACKET (expression (COMMA expression)*)? RBRACKET -> ^(List expression*);

///////////////////////////////////////////////////////////////////////////////
// TOKENS

ALL: 'all';
ACTOR: 'actor';
END: 'end';
ENTITIES: 'entities';
IMPORT: 'import';
MULTI: 'multi';
MUTABLE: 'mutable';
NETWORK: 'network';
STRUCTURE: 'structure';
NOT: 'not';
VAR: 'var';

TRUE: 'true';
FALSE: 'false';

ID: ('a'..'z' | 'A'..'Z' | '_' | '$') ('a'..'z' | 'A'..'Z' | '_' | '$' | '0' .. '9')* ;
FLOAT: '-'? (('0'..'9')+ '.' ('0'..'9')* (('e' | 'E') ('+' | '-')? ('0'..'9')+)?
	| '.' ('0'..'9')+ (('e' | 'E') ('+' | '-')? ('0'..'9')+)?
	| ('0'..'9')+ (('e' | 'E') ('+' | '-')? ('0'..'9')+));
INTEGER: '-'? ('0'..'9')+ ;
STRING: '\"' .* '\"';

LINE_COMMENT: '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;};
MULTI_LINE_COMMENT: '/*' .* '*/' {$channel=HIDDEN;};
WHITESPACE: (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;};

EQ: '=';
GE: '>=';
GT: '>';
LE: '<=';
LT: '<';
NE: '!=';

ARROW: '->';
COLON: ':';
COLON_EQUAL: ':=';
COMMA: ',';
DOT: '.';
DOUBLE_DASH_ARROW: '-->';
DOUBLE_EQUAL_ARROW: '==>';
DOUBLE_DOT: '..';

LBRACE: '{';
RBRACE: '}';
LBRACKET: '[';
RBRACKET: ']';
LPAREN: '(';
RPAREN: ')';

XOR: '^';
AND: '&';
OR: '|';
DIV: '/';
MINUS: '-';
PLUS: '+';
TIMES: '*';

SEMICOLON: ';';
SHARP: '#';

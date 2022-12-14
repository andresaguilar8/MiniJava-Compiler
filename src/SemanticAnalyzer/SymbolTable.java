package SemanticAnalyzer;

import AST.Sentence.BlockNode;
import AST.Sentence.LocalVarDeclarationNode;
import LexicalAnalyzer.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class SymbolTable {

    private Class currentClass;
    private Method currentMethod;
    private static SymbolTable instance = null;
    private Hashtable<String, ConcreteClass> concreteClassesTable;
    private Hashtable<String, Interface> interfacesTable;
    private boolean mainMethodIsDeclared;
    private Token EOFToken;
    private ArrayList<SemanticError> semanticErrorsList;
    private BlockNode currentBlock;
    private Method mainMethod;
    private int interfaceMethodOffset;

    public static SymbolTable getInstance() {
        if (instance == null)
            instance = new SymbolTable();
        return instance;
    }

    private SymbolTable() {
        this.concreteClassesTable = new Hashtable<String, ConcreteClass>();
        this.interfacesTable = new Hashtable<String, Interface>();
        this.mainMethodIsDeclared = false;
        this.semanticErrorsList = new ArrayList<>();
        this.initPredefinedClasses();
    }

    public ArrayList<SemanticError> getSemanticErrorsList() {
        return this.semanticErrorsList;
    }

    public void insertConcreteClass(ConcreteClass classToInsert) throws SemanticException {
        if (!this.concreteClassesTable.containsKey(classToInsert.getClassName()) && !this.interfacesTable.containsKey(classToInsert.getClassName())) {
            this.concreteClassesTable.put(classToInsert.getClassName(), classToInsert);
        } else
            SymbolTable.getInstance().getSemanticErrorsList().add(new SemanticError(classToInsert.getToken(), "El nombre " + classToInsert.getClassName() + " ya esta declarado"));
    }

    public void insertInterface(Interface interfaceToInsert) throws SemanticException {
        if (!this.concreteClassesTable.containsKey(interfaceToInsert.getClassName()) && !this.interfacesTable.containsKey(interfaceToInsert.getClassName())) {
            this.interfacesTable.put(interfaceToInsert.getClassName(), interfaceToInsert);
        } else
            SymbolTable.getInstance().getSemanticErrorsList().add(new SemanticError(interfaceToInsert.getToken(), "El nombre " + interfaceToInsert.getClassName() + " ya esta declarado"));
    }

    public Class getCurrentClass() {
        return this.currentClass;
    }

    public void setActualClass(Class currentClass) {
        this.currentClass = currentClass;
    }

    public void setCurrentMethod(Method currentMethod) {
        this.currentMethod = currentMethod;
    }

    public Method getCurrentMethod() {
        return this.currentMethod;
    }

    public Hashtable<String, ConcreteClass> getConcreteClassesTable() {
        return this.concreteClassesTable;
    }

    public Hashtable<String, Interface> getInterfacesTable() {
        return this.interfacesTable;
    }

    public boolean concreteClassIsDeclared(String className) {
        return SymbolTable.getInstance().getConcreteClassesTable().containsKey(className);
    }

    public boolean interfaceIsDeclared(String interfaceName) {
        return SymbolTable.getInstance().getInterfacesTable().containsKey(interfaceName);
    }

    public Class getClass(String name) {
        for (ConcreteClass concreteClass: this.concreteClassesTable.values())
            if (concreteClass.getClassName().equals(name))
                return concreteClass;
        for (Interface i: this.interfacesTable.values())
            if (i.getClassName().equals(name))
                return i;
        return null;
    }

    public ConcreteClass getConcreteClass(String concreteClassName) {
        return SymbolTable.getInstance().getConcreteClassesTable().get(concreteClassName);
    }

    public Interface getInterface(String interfaceName) {
        return SymbolTable.getInstance().getInterfacesTable().get(interfaceName);
    }

    public void checkDeclarations() throws SemanticException {
        for (ConcreteClass classToCheck : this.concreteClassesTable.values()) {
            classToCheck.checkDeclarations();
            this.checkMainMethod(classToCheck);
        }
        for (Interface interfaceToCheck : this.interfacesTable.values())
            interfaceToCheck.checkDeclarations();
    }

    public void setCurrentBlock(BlockNode currentBlock) {
        this.currentBlock = currentBlock;
    }

    public BlockNode getCurrentBlock() {
        return this.currentBlock;
    }

    public void checkSentences() throws SemanticExceptionSimple {
        for (ConcreteClass concreteClass: this.concreteClassesTable.values()) {
            this.currentClass = concreteClass;
            for (Method method : concreteClass.getMethods().values()) {
                this.currentMethod = method;
                if (!method.isChecked()) {
                    if (method.getPrincipalBlock() != null) {
                        this.setCurrentBlock(method.getPrincipalBlock());
                        method.getPrincipalBlock().check();
                    }
                    method.setChecked();
                }
            }
        }
    }

    public boolean isMethodParameter(String varName, Method method){
        for (Parameter parameter: method.getParametersList())
            if (parameter.getParameterName().equals(varName))
                return true;
        return false;
    }

    public Parameter retrieveParameter(String varName, Method method) {
        boolean foundParameter = false;
        ArrayList<Parameter> methodParameterList = method.getParametersList();
        int listIndex = 0;
        while (!foundParameter) {
            Parameter parameter = methodParameterList.get(listIndex);
            if (parameter.getParameterName().equals(varName)) {
                foundParameter = true;
                return parameter;
            }
            listIndex += 1;
        }
        return null;
    }

    public Type retrieveParameterType(String varName, Method method) {
        boolean foundParameter = false;
        ArrayList<Parameter> methodParameterList = method.getParametersList();
        int listIndex = 0;
        Type typeToReturn = null;
        while (!foundParameter) {
            Parameter parameter = methodParameterList.get(listIndex);
            if (parameter.getParameterName().equals(varName)) {
                typeToReturn =  parameter.getParameterType();
                foundParameter = true;
            }
            listIndex += 1;
        }
        return typeToReturn;
    }

    public boolean isAttribute(String varName, ConcreteClass concreteClass) {
        if (concreteClass.getAttributes().containsKey(varName))
            return true;
        return false;
    }

    public boolean isPublicAttribute(String varName, ConcreteClass concreteClass) {
        if (concreteClass.getAttributes().containsKey(varName)) {
            Attribute attribute = concreteClass.getAttributes().get(varName);
            return attribute.getVisibility().equals("public");
        }
        return false;
    }

    public Type retrieveAttribute(String varName, ConcreteClass concreteClass) {
        return concreteClass.getAttributes().get(varName).getAttributeType();
    }

    public boolean isCurrentBlockLocalVar(String varName) {
        BlockNode currentBlockAncestor = this.currentBlock;
        if (!currentBlockAncestor.getLocalVarTable().containsKey(varName))
            while (currentBlockAncestor.getAncestorBlock() != null) {
                currentBlockAncestor = currentBlockAncestor.getAncestorBlock();
                if (currentBlockAncestor.getLocalVarTable().containsKey(varName))
                    return true;
            }
        else
            return true;
        return false;
    }

    public LocalVarDeclarationNode retrieveLocalVar(String varName) {
        BlockNode currentBlockAncestor = this.currentBlock;
        if (!currentBlockAncestor.getLocalVarTable().containsKey(varName))
            while (currentBlockAncestor.getAncestorBlock() != null) {
                currentBlockAncestor = currentBlockAncestor.getAncestorBlock();
                if (currentBlockAncestor.getLocalVarTable().containsKey(varName))
                    return currentBlockAncestor.getLocalVarTable().get(varName);
            }
        else
            return currentBlockAncestor.getLocalVarTable().get(varName);
        return null;
    }

    private void checkMainMethod(ConcreteClass classToCheck) {
        for (Method methodToCheck : classToCheck.getMethods().values())
            if (methodToCheck.getStaticHeader().equals("static") && methodToCheck.getReturnTypeString().equals("void") && methodToCheck.getMethodName().equals("main") && !methodToCheck.hasParameters())
                if (this.mainMethodIsDeclared == true)
                    SymbolTable.getInstance().getSemanticErrorsList().add(new SemanticError(methodToCheck.getMethodToken(), "Ya existe un metodo main estatico y sin parametros"));
                else {
                    this.mainMethodIsDeclared = true;
                    this.mainMethod = methodToCheck;
                }
    }

    public void consolidate() throws SemanticException, IOException {
        this.consolidateInterfaces();
        this.consolidateConcreteClasses();
        if (!this.mainMethodIsDeclared)
            SymbolTable.getInstance().getSemanticErrorsList().add(new SemanticError(this.EOFToken, "No se encontro el metodo estatico main sin parametros declarado dentro de ninguna clase"));
        this.generateMethodsOffsets();
    }

    private void consolidateInterfaces() {
        for (Interface interfaceToConsolidate : this.interfacesTable.values())
            interfaceToConsolidate.consolidate();
    }

    private void consolidateConcreteClasses() {
        for (ConcreteClass classToConsolidate : this.concreteClassesTable.values())
            classToConsolidate.consolidate();
    }

    private void generateMethodsOffsets(){
        for (ConcreteClass concreteClass : this.concreteClassesTable.values()) {
            concreteClass.generateAttributesOffsets();
            concreteClass.generateClassMethodsOffsets();
        }

        this.generateInterfacesMethodsOffsets();

        for (ConcreteClass concreteClass : this.concreteClassesTable.values())
            concreteClass.generateInterfaceMethodsOffsets();

        for (ConcreteClass concreteClass : this.concreteClassesTable.values())
            concreteClass.generateInheritedMethodsOffsetsForVt();
    }

    private void generateInterfacesMethodsOffsets() {
        this.interfaceMethodOffset = this.getGreatestVtSize();

        for (Interface interfaceWithoutAncestors: this.interfacesTable.values())
            if (!interfaceWithoutAncestors.hasAncestors())
                this.generateInterfaceMethodsOffsets(interfaceWithoutAncestors);

        for (Interface interfaceWithAncestors: this.interfacesTable.values())
            if (interfaceWithAncestors.hasAncestors())
                this.generateInterfaceMethodsOffsetsForInterfaceWithAncestors(interfaceWithAncestors);
    }

    private int getGreatestVtSize() {
        int greatestVtSize = 0;
        for (ConcreteClass concreteClass: this.concreteClassesTable.values()) {
            if (concreteClass.getAncestorsInterfaces().size() > 0)
                if (concreteClass.getVtSize() > greatestVtSize)
                    greatestVtSize = concreteClass.getVtSize();
        }
        return greatestVtSize + 1;
    }

    private void generateInterfaceMethodsOffsets(Interface interfaceToGenerateMethodsOffsets) {
        for (Method interfaceMethod: interfaceToGenerateMethodsOffsets.getMethods().values()) {
            if (!interfaceMethod.hasOffset()) {
                interfaceMethod.setOffset(interfaceMethodOffset);
                interfaceMethod.setOffsetIsSet();
                interfaceMethodOffset = interfaceMethodOffset + 1;
            }
        }
        interfaceToGenerateMethodsOffsets.setOffsetsAsSet();
    }

    private void generateInterfaceMethodsOffsetsForInterfaceWithAncestors(Interface interfaceWithAncestors) {
        for (Interface ancestorInterface: interfaceWithAncestors.getAncestorsInterfaces()) {
            Interface ancestorInterfaceInST = SymbolTable.instance.getInterface(ancestorInterface.getClassName());
            if (ancestorInterfaceInST.hasOffsetsGenerated()) {
                this.copyMethodOffsets(ancestorInterfaceInST, interfaceWithAncestors);
            }
            else {
                generateInterfaceMethodsOffsetsForInterfaceWithAncestors(ancestorInterfaceInST);
            }
        }
        this.generateInterfaceMethodsOffsets(interfaceWithAncestors);
        interfaceWithAncestors.setOffsetsAsSet();
    }

    private void copyMethodOffsets(Interface interfaceWithOffsets, Interface interfaceToCopyOffsetsIn) {
        for (Method methodInInterfaceWithOffsets: interfaceWithOffsets.getMethods().values()) {
            Method methodToSetOffset = interfaceToCopyOffsetsIn.getMethod(methodInInterfaceWithOffsets.getMethodName());
            methodToSetOffset.setOffset(methodInInterfaceWithOffsets.getOffset());
            methodToSetOffset.setOffsetIsSet();
        }
    }

    public void printClassesAndInterfacesMethodsOffsets() throws IOException {
        System.out.println();
        for (ConcreteClass concreteClass : this.concreteClassesTable.values()) {
            if (!concreteClass.getClassName().equals("Object"))
                if (!concreteClass.getClassName().equals("System"))
                    if (!concreteClass.getClassName().equals("String")) {
                        System.out.println();
                        System.out.println("Clase: " + concreteClass.getClassName());
                        for (Method method : concreteClass.getMethods().values()) {
                            if (!method.isStatic())
                                System.out.println("metodo: " + method.getMethodName() + " con offset: " + method.getOffset());
                        }
                    }

        }
        System.out.println();
        for (Interface i: this.interfacesTable.values()) {
            System.out.println();
            System.out.println("Interface: " + i.getClassName());
            for (Method method : i.getMethods().values())
                System.out.println("metodo: " + method.getMethodName() + " con offset: " + method.getOffset());
        }
    }

    private void initPredefinedClasses() {
        this.initObjectClass();
        this.initStringClass();
        this.initSystemClass();
    }

    public void emptySymbolTable() {
        this.concreteClassesTable = new Hashtable<String, ConcreteClass>();
        this.interfacesTable = new Hashtable<String, Interface>();
        this.mainMethodIsDeclared = false;
        this.semanticErrorsList = new ArrayList<>();
        this.initPredefinedClasses();
    }

    public void setEOFToken(Token EOFToken) {
        this.EOFToken = EOFToken;
    }

    private void initObjectClass() {
        Token objectClassToken = new Token("idClase", "Object", 0);
        Token debugPrintMethodToken = new Token("idMV", "debugPrint", 0);
        Token voidToken = new Token("pr_void", "void", 0);
        Token intToken = new Token("pr_int", "int", 0);
        Token parameterToken = new Token("idMV", "i", 0);

        Type debugPrintMethodType = new PrimitiveType(voidToken);
        Type debugPrintMethodParameterType = new PrimitiveType(intToken);

        Parameter methodParameter = new Parameter(parameterToken, debugPrintMethodParameterType);
        ConcreteClass objectClass = new ConcreteClass(objectClassToken, null);
        objectClass.setConsolidated();
        Method debugPrintMethod = new Method(debugPrintMethodToken, "static", debugPrintMethodType, objectClass.getClassName());

        debugPrintMethod.insertParameter(methodParameter);
        objectClass.insertMethod(debugPrintMethod);
        this.concreteClassesTable.put(objectClass.getClassName(), objectClass);

    }

    private void initStringClass() {
        Token stringClassToken = new Token("idClase", "String", 0);
        ConcreteClass ancestorClass = this.concreteClassesTable.get("Object");
        Token ancestorClassToken = ancestorClass.getToken();

        ConcreteClass stringConcreteClass = new ConcreteClass(stringClassToken, ancestorClassToken);
        this.concreteClassesTable.put(stringConcreteClass.getClassName(), stringConcreteClass);
    }

    private void initSystemClass() {
        Token systemClassToken = new Token("idClase", "System", 0);
        ConcreteClass ancestorClass = this.concreteClassesTable.get("Object");
        Token ancestorClassToken = ancestorClass.getToken();
        ConcreteClass systemConcreteClass = new ConcreteClass(systemClassToken, ancestorClassToken);
        this.concreteClassesTable.put(systemConcreteClass.getClassName(), systemConcreteClass);
        this.insertReadMethod(systemConcreteClass);
        this.insertPrintBMethod(systemConcreteClass);
        this.insertPrintCMethod(systemConcreteClass);
        this.insertPrintIMethod(systemConcreteClass);
        this.insertPrintSMethod(systemConcreteClass);
        this.insertPrintlnMethod(systemConcreteClass);
        this.insertPrintBlnMethod(systemConcreteClass);
        this.insertPrintClnMethod(systemConcreteClass);
        this.insertPrintIlnMethod(systemConcreteClass);
        this.insertPrintSlnMethod(systemConcreteClass);
    }

    private void insertReadMethod(ConcreteClass concreteClass) {
        Token intToken = new Token("pr_int", "int", 0);
        Type readMethodType = new PrimitiveType(intToken);
        Token readMethodToken = new Token("idMV", "read", 0);
        Method readMethod = new Method(readMethodToken, "static", readMethodType, concreteClass.getClassName());
        concreteClass.insertMethod(readMethod);
    }

    private void insertPrintBMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token booleanToken = new Token("pr_boolean", "boolean", 0);
        Token parameterBToken = new Token("idMV", "b", 0);
        Type printBMethodType = new PrimitiveType(voidToken);
        Token printBMethodToken = new Token("idMV", "printB", 0);
        Method printBMethod = new Method(printBMethodToken, "static", printBMethodType, concreteClass.getClassName());
        Type printBMethodParameterType = new PrimitiveType(booleanToken);
        Parameter parameterB = new Parameter(parameterBToken, printBMethodParameterType);
        printBMethod.insertParameter(parameterB);
        concreteClass.insertMethod(printBMethod);
    }

    private void insertPrintCMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token charToken = new Token("pr_char", "char", 0);
        Token parameterCToken = new Token("idMV", "c", 0);
        Type printCMethodType = new PrimitiveType(voidToken);
        Token printCMethodToken = new Token("idMV", "printC", 0);
        Method printCMethod = new Method(printCMethodToken, "static", printCMethodType, concreteClass.getClassName());
        Type printCMethodParameterType = new PrimitiveType(charToken);
        Parameter parameterC = new Parameter(parameterCToken, printCMethodParameterType);
        printCMethod.insertParameter(parameterC);
        concreteClass.insertMethod(printCMethod);
    }

    private void insertPrintIMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token intToken = new Token("pr_int", "int", 0);
        Token parameterIToken = new Token("idMV", "i", 0);
        Type printIMethodType = new PrimitiveType(voidToken);
        Token printIMethodToken = new Token("idMV", "printI", 0);
        Method printIMethod = new Method(printIMethodToken, "static", printIMethodType, concreteClass.getClassName());
        Type printIMethodParameterType = new PrimitiveType(intToken);
        Parameter parameterI = new Parameter(parameterIToken, printIMethodParameterType);
        printIMethod.insertParameter(parameterI);
        concreteClass.insertMethod(printIMethod);
    }

    private void insertPrintSMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token stringToken = new Token("idClase", "String", 0);
        Token parameterSToken = new Token("idMV", "s", 0);
        Type printSMethodType = new PrimitiveType(voidToken);
        Token printSMethodToken = new Token("idMV", "printS", 0);
        Method printSMethod = new Method(printSMethodToken, "static", printSMethodType, concreteClass.getClassName());
        Type printIMethodParameterType = new PrimitiveType(stringToken);
        Parameter parameterS = new Parameter(parameterSToken, printIMethodParameterType);
        printSMethod.insertParameter(parameterS);
        concreteClass.insertMethod(printSMethod);
    }

    private void insertPrintlnMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Type printlnMethodType = new PrimitiveType(voidToken);
        Token printlnMethodToken = new Token("idMV", "println", 0);
        Method printlnMethod = new Method(printlnMethodToken, "static", printlnMethodType, concreteClass.getClassName());
        concreteClass.insertMethod(printlnMethod);
    }

    private void insertPrintBlnMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token booleanToken = new Token("pr_boolean", "boolean", 0);
        Token parameterBToken = new Token("idMV", "b", 0);
        Type printBlnMethodType = new PrimitiveType(voidToken);
        Token printBlnMethodToken = new Token("idMV", "printBln", 0);
        Method printBlnMethod = new Method(printBlnMethodToken, "static", printBlnMethodType, concreteClass.getClassName());
        Type printBlnMethodParameterType = new PrimitiveType(booleanToken);
        Parameter parameterB = new Parameter(parameterBToken, printBlnMethodParameterType);
        printBlnMethod.insertParameter(parameterB);
        concreteClass.insertMethod(printBlnMethod);
    }

    private void insertPrintClnMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token charToken = new Token("pr_char", "char", 0);
        Token parameterCToken = new Token("idMV", "c", 0);
        Type printClnMethodType = new PrimitiveType(voidToken);
        Token printClnMethodToken = new Token("idMV", "printCln", 0);
        Method printClnMethod = new Method(printClnMethodToken, "static", printClnMethodType, concreteClass.getClassName());
        Type printClnMethodParameterType = new PrimitiveType(charToken);
        Parameter parameterB = new Parameter(parameterCToken, printClnMethodParameterType);
        printClnMethod.insertParameter(parameterB);
        concreteClass.insertMethod(printClnMethod);
    }

    private void insertPrintIlnMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token intToken = new Token("pr_int", "int", 0);
        Token parameterIToken = new Token("idMV", "i", 0);
        Type printIlnMethodType = new PrimitiveType(voidToken);
        Token printIlnMethodToken = new Token("idMV", "printIln", 0);
        Method printIlnMethod = new Method(printIlnMethodToken, "static", printIlnMethodType, concreteClass.getClassName());
        Type printIlnMethodParameterType = new PrimitiveType(intToken);
        Parameter parameterI = new Parameter(parameterIToken, printIlnMethodParameterType);
        printIlnMethod.insertParameter(parameterI);
        concreteClass.insertMethod(printIlnMethod);
    }

    private void insertPrintSlnMethod(ConcreteClass concreteClass) {
        Token voidToken = new Token("pr_void", "void", 0);
        Token stringToken = new Token("idClase", "String", 0);
        Token parameterSToken = new Token("idMV", "s", 0);
        Type printSlnMethodType = new PrimitiveType(voidToken);
        Token printSlnMethodToken = new Token("idMV", "printSln", 0);
        Method printSlnMethod = new Method(printSlnMethodToken, "static", printSlnMethodType, concreteClass.getClassName());
        Type printSlnMethodParameterType = new PrimitiveType(stringToken);
        Parameter parameterS = new Parameter(parameterSToken, printSlnMethodParameterType);
        printSlnMethod.insertParameter(parameterS);
        concreteClass.insertMethod(printSlnMethod);
    }

    public Method getMainMethod() {
        return this.mainMethod;
    }
}


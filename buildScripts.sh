compile() {
    java -cp build/libs/Triangle-Tools.jar triangle.Compiler "$@"
}

run() {
    java -cp build/libs/Triangle-Tools.jar triangle.abstractMachine.Interpreter "$@"
}
runD() {
    java -Djava.compiler=NONE -cp build/libs/Triangle-Tools.jar triangle.abstractMachine.Interpreter "$@"
}
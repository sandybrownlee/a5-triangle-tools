compile() {
    java -cp build/libs/Triangle-Tools.jar triangle.Compiler "$@"
}

run() {
    java -cp build/libs/Triangle-Tools.jar triangle.abstractMachine.Interpreter "$@"
}
runD() {
    java -Djava.compiler=NONE -cp build/libs/Triangle-Tools.jar triangle.abstractMachine.Interpreter "$@"
}

testHoist() {
    compile programs/$1.tri -o $1.tam
    compile programs/$1.tri -o $1-folded.tam -f
    compile programs/$1.tri -o $1-hoisted.tam -h
}

runHoist() {
    echo "Normal"
    run $1.tam
    echo ""
    echo "Folded"
    run $1-folded.tam
    echo ""
    echo "Hoisted"
    run $1-hoisted.tam
}

runHoistD() {
    echo "Normal"
    runD $1.tam
    echo ""
    echo "Folded"
    runD $1-folded.tam
    echo ""
    echo "Hoisted"
    runD $1-hoisted.tam
}
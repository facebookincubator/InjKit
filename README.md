# InjKit is a framework for bytecode manipulation.

InjKit is a framework for bytecode manipulation based on ASM library.
It has plugin-like architecture and allows to process each class and method.
The plugins or injectors can add extra bytecode like try...catch... blocks, calls to logger etc
based on java annotations or name of the class or method.
It can be easily integrated as part of build workflow as Gradle Plugin or via CLI.

## License
InjKit is [MIT-licensed](https://github.com/facebookincubator/InjKit/blob/master/LICENSE).

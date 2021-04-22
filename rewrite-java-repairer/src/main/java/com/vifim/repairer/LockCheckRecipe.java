/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vifim.repairer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J.Modifier.Type;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.tree.J.MethodDeclaration;
import java.util.stream.Collectors;
import java.util.List;

public class LockCheckRecipe extends Recipe {
    // Making your recipe immutable helps make them idempotent and eliminates categories of possible bugs
    // Configuring your recipe in this way also guarantees that basic validation of parameters will be done for you by rewrite
    // @Option(displayName = "Fully Qualified Class Name",
    //         description = "A fully-qualified class name indicating which class to detect lock() method.",
    //         example = "com.yourorg.FooBar")
    // @NonNull
    // private final String fullyQualifiedMethodName;

    // Recipes must be serializable. This is verified by RecipeTest.assertChanged() and RecipeTest.assertUnchanged()
    // @JsonCreator
    // public LockCheckRecipe(@NonNull @JsonProperty("fullyQualifiedMethodName") String fullyQualifiedMethodName) {
    //     this.fullyQualifiedMethodName = fullyQualifiedMethodName;
    // }

    @Override
    public String getDisplayName() {
        return "Check Lock()";
    }

    @Override
    public String getDescription() {
        return "Adds a \"hello\" method to the specified class";
    }

    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new LockCheckVisitor();
    }

    public class LockCheckVisitor extends JavaIsoVisitor<ExecutionContext> {

        //This visitor uses a method matcher, and it's point-cut syntax, to target the method declaration that will be refactored
        private MethodMatcher methodMatcher = new MethodMatcher("repairer.LockTest testLock()");

        private final JavaTemplate unLockTemplate = template("lock.unlock();").build();

        @Override
        public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, ExecutionContext c) {
            J.MethodDeclaration m = super.visitMethodDeclaration(method, c);

            System.out.println("1. method.getName():"+m.getName().getSimpleName());//test
            
            if (!methodMatcher.matches(method.getType())) {
                return m;
            }

            System.out.println("2. method.getName():"+m.getName().getSimpleName());//test
            // System.out.println("3. methodInvocation.getName():"+m.getName().getSimpleName());//test

            // Check if the method contains lock
            boolean lockMethodExists = m.getBody().getStatements().stream()
            .filter(statement -> statement instanceof J.MethodInvocation)
            .map(J.MethodInvocation.class::cast)
            .anyMatch(methodInvocation -> methodInvocation.getName().getSimpleName().equals("lock"));
            if (!lockMethodExists) {
                return m;
            }

            // Check if the method contains unlock
            boolean unlockMethodExists = m.getBody().getStatements().stream()
            .filter(statement -> statement instanceof J.MethodInvocation)
            .map(J.MethodInvocation.class::cast)
            .anyMatch(methodInvocation -> methodInvocation.getName().getSimpleName().equals("unlock"));
            if (unlockMethodExists) {
                return m;
            }

            List<J.MethodInvocation> results = m.getBody().getStatements().stream()
            .filter(statement -> statement instanceof J.MethodInvocation)
            .map(J.MethodInvocation.class::cast)
            .collect(Collectors.toList());
            // System.out.println("method.getType():"+method.getType());
            System.out.println("3. results.size():"+results.size());//test

            for(int i=0;i<results.size();i++){
                m = m.withTemplate(unLockTemplate, results.get(i).getCoordinates().before());
            }
            
            System.out.println(results.get(0).getCoordinates().before());
            // flag++;
            // if(flag==1){
            //     //Remove the abstract modifier from the method.
            //     // m = m.withModifiers(m.getModifiers().stream().filter(mod -> mod.getType() != Type.Abstract).collect(Collectors.toList()));
            //     m = m.withModifiers(ListUtils.map(m.getModifiers(),mod -> mod.getType() == J.Modifier.Type.Abstract ? null : mod));
            //     //Add a method body use the JavaTemplate by using the "replaceBody" coordinates.
            //     m = m.withTemplate(addMethodBodyTemplate, m.getCoordinates().replaceBody());
            //     return m;
            // }else if(flag==2){
            //     //Add two parameters to the method declaration by inserting them in from of the first argument.
            //     m = m.withTemplate(addMethodParametersTemplate, m.getParameters().get(0).getCoordinates().before());
            //     //Add two additional statements to method's body by inserting them in front of the first statement
            //     m = m.withTemplate(addStatementsTemplate, m.getBody().getStatements().get(0).getCoordinates().before());
            //     //Need to make sure that the Date type is added to this compilation unit's list of imports.
            //     maybeAddImport("java.util.Date");
            //     return m;
            // }else{
            //     return m;
            // }
            return m;
        }
    }
}
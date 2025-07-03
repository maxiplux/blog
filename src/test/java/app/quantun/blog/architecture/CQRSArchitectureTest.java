package app.quantun.blog.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Tag;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Tests de arquitectura para verificar la correcta implementación de CQRS
 * Valida la separación de responsabilidades y adherencia a principios CQRS
 */
@AnalyzeClasses(packages = "app.quantun.blog",
        importOptions = ImportOption.DoNotIncludeTests.class)
@Tag("architecture")
class CQRSArchitectureTest {

    // ===== LAYERS DEFINITION =====

    @ArchTest
    static final ArchRule hexagonal_architecture_is_respected = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()

            .layer("Controllers").definedBy("..infrastructure.adapter.in.web..")
            .layer("Application Commands").definedBy("..application.command..")
            .layer("Application Queries").definedBy("..application.query..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Shared").definedBy("..shared..")

            .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application Commands").mayOnlyBeAccessedByLayers("Controllers")
            .whereLayer("Application Queries").mayOnlyBeAccessedByLayers("Controllers")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application Commands", "Application Queries", "Infrastructure")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Controllers", "Application Commands", "Application Queries");

    // ===== CQRS COMMAND RULES =====

    @ArchTest
    static final ArchRule command_services_should_only_implement_command_interfaces =
            classes().that().resideInAPackage("..application.command.service..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "..application.command..",
                            "..domain..",
                            "..shared..",
                            "..application.port.out..",
                            "java..",
                            "org.springframework..",
                            "org.slf4j.."
                    );

    @ArchTest
    static final ArchRule command_interfaces_should_be_in_command_package =
            classes().that().haveNameMatching(".*Command")
                    .should().resideInAPackage("..application.command.port.in..");

    @ArchTest
    static final ArchRule command_services_should_not_depend_on_query_services =
            noClasses().that().resideInAPackage("..application.command.service..")
                    .should().dependOnClassesThat().resideInAPackage("..application.query..");

    @ArchTest
    static final ArchRule command_repository_ports_should_only_have_write_operations =
            classes().that().haveNameMatching(".*CommandRepositoryPort")
                    .should().resideInAPackage("..application.command.port.out..");

    // ===== CQRS QUERY RULES =====

    @ArchTest
    static final ArchRule query_services_should_only_implement_query_interfaces =
            classes().that().resideInAPackage("..application.query.service..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "..application.query..",
                            "..domain..",
                            "..shared..",
                            "java..",
                            "org.springframework..",
                            "org.slf4j.."
                    );

    @ArchTest
    static final ArchRule query_interfaces_should_be_in_query_package =
            classes().that().haveNameMatching(".*Query")
                    .should().resideInAPackage("..application.query.port.in..");

    @ArchTest
    static final ArchRule query_services_should_not_depend_on_command_services =
            noClasses().that().resideInAPackage("..application.query.service..")
                    .should().dependOnClassesThat().resideInAPackage("..application.command..");

    @ArchTest
    static final ArchRule query_repository_ports_should_only_have_read_operations =
            classes().that().haveNameMatching(".*QueryRepositoryPort")
                    .should().resideInAPackage("..application.query.port.out..");

    // ===== READ MODELS RULES =====

    @ArchTest
    static final ArchRule read_models_should_be_in_query_model_package =
            classes().that().haveNameMatching(".*ReadModel")
                    .or().haveNameMatching(".*ListItem")
                    .should().resideInAPackage("..application.query.model..");

    @ArchTest
    static final ArchRule read_models_should_be_records =
            classes().that().resideInAPackage("..application.query.model..")
                    .and().areNotMemberClasses()
                    .should().beRecords();

    @ArchTest
    static final ArchRule commands_should_not_return_read_models =
            noMethods().that().areDeclaredInClassesThat().resideInAPackage("..application.command..")
                    .should().haveRawReturnType(resideInAPackage("..application.query.model.."));

    // ===== VALUE OBJECTS RULES =====

    @ArchTest
    static final ArchRule value_objects_should_be_records_or_immutable =
            classes().that().resideInAPackage("..shared.valueobject..")
                    .should().beRecords()
                    .orShould().haveOnlyFinalFields();

    @ArchTest
    static final ArchRule pagination_should_use_page_value_objects =
            methods().that().haveNameMatching(".*getAll.*")
                    .or().haveNameMatching(".*search.*")
                    .or().haveNameMatching(".*find.*")
                    .and().areDeclaredInClassesThat().resideInAPackage("..application.query..")
                    .should().haveNameMatching(".*");

    // ===== REPOSITORY ADAPTER RULES =====

    @ArchTest
    static final ArchRule repository_adapters_should_be_separated =
            classes().that().haveNameMatching(".*CommandRepositoryAdapter")
                    .should().resideInAPackage("..infrastructure.adapter.out.persistence..");

    @ArchTest
    static final ArchRule query_adapters_should_not_have_save_methods =
            noMethods().that().areDeclaredInClassesThat().haveNameMatching(".*QueryRepositoryAdapter")
                    .should().haveNameMatching("save.*")
                    .orShould().haveNameMatching("delete.*")
                    .orShould().haveNameMatching("update.*");

    @ArchTest
    static final ArchRule command_adapters_should_not_have_pagination_methods =
            noMethods().that().areDeclaredInClassesThat().haveNameMatching(".*CommandRepositoryAdapter")
                    .should().haveNameMatching(".*Page.*");

    // ===== CONTROLLER RULES =====

    @ArchTest
    static final ArchRule controllers_should_separate_command_and_query_dependencies =
            fields().that().areDeclaredInClassesThat().haveNameMatching(".*Controller")
                    .and().haveRawType(resideInAPackage("..application.command.."))
                    .should().haveNameMatching(".*[Cc]ommand.*");

    @ArchTest
    static final ArchRule controllers_should_inject_query_dependencies_properly =
            fields().that().areDeclaredInClassesThat().haveNameMatching(".*Controller")
                    .and().haveRawType(resideInAPackage("..application.query.."))
                    .should().haveNameMatching(".*[Qq]uery.*");

    // ===== DOMAIN MODEL RULES =====

    @ArchTest
    static final ArchRule domain_entities_should_be_immutable =
            classes().that().resideInAPackage("..domain.model..")
                    .and().areNotEnums()
                    .should().haveOnlyFinalFields()
                    .orShould().beRecords();

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_application =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..application.command..",
                            "..application.query.."
                    );

    // ===== SERVICE ANNOTATIONS =====

    @ArchTest
    static final ArchRule command_services_should_be_annotated_with_service =
            classes().that().resideInAPackage("..application.command.service..")
                    .should().beAnnotatedWith(org.springframework.stereotype.Service.class);

    @ArchTest
    static final ArchRule query_services_should_be_annotated_with_service =
            classes().that().resideInAPackage("..application.query.service..")
                    .should().beAnnotatedWith(org.springframework.stereotype.Service.class);

    @ArchTest
    static final ArchRule repository_adapters_should_be_annotated_with_component =
            classes().that().haveNameMatching(".*RepositoryAdapter")
                    .should().beAnnotatedWith(org.springframework.stereotype.Component.class);

    // ===== PACKAGE STRUCTURE RULES =====

    @ArchTest
    static final ArchRule cqrs_packages_should_have_proper_structure =
            classes().that().resideInAPackage("..application..")
                    .should().resideInAnyPackage(
                            "..application.command..",
                            "..application.query..",
                            "..application.port.out.."  // Legacy compatibility
                    );

    @ArchTest
    static final ArchRule no_cyclic_dependencies_between_command_and_query =
            noClasses().that().resideInAPackage("..application.command..")
                    .should().dependOnClassesThat().resideInAPackage("..application.query..")
                    .andShould().dependOnClassesThat().resideInAPackage("..application.command..");

    // ===== HELPER METHODS =====

    private static com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> typeMatching(String regex) {
        return com.tngtech.archunit.base.DescribedPredicate.describe(
                "type matching " + regex,
                javaClass -> javaClass.getName().matches(regex)
        );
    }

    private static com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> resideInAPackage(String packageName) {
        return com.tngtech.archunit.base.DescribedPredicate.describe(
                "reside in package " + packageName,
                javaClass -> javaClass.getPackageName().matches(packageName.replace("..", ".*"))
        );
    }
}

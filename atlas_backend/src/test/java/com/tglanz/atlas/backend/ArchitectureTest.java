package com.tglanz.atlas.backend;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.tglanz.atlas.backend",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule domainIsFrameworkFree =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..", "jakarta..", "..application..", "..adapter..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapter..", "jakarta.persistence..", "org.springframework.web..")
          .orShould()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework.data..");

  @ArchTest
  static final ArchRule inboundAdaptersDoNotTouchOutboundAdapters =
      noClasses()
          .that()
          .resideInAPackage("..adapter.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter.out..");

  @ArchTest
  static final ArchRule movementDoesNotDependOnRoutine =
      noClasses()
          .that()
          .resideInAPackage("com.tglanz.atlas.backend.movement..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.tglanz.atlas.backend.routine..");

  @ArchTest
  static final ArchRule routineOnlyUsesMovementThroughItsOutboundAdapter =
      noClasses()
          .that()
          .resideInAPackage("com.tglanz.atlas.backend.routine..")
          .and()
          .resideOutsideOfPackage("..routine.adapter.out.movement..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.tglanz.atlas.backend.movement..");

  @ArchTest
  static final ArchRule routineAdapterUsesOnlyMovementPorts =
      noClasses()
          .that()
          .resideInAPackage("..routine.adapter.out.movement..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.tglanz.atlas.backend.movement.adapter..",
              "com.tglanz.atlas.backend.movement.application.service..");

  /** Features talk to each other only through an outbound adapter named after the other feature. */
  @ArchTest
  static final ArchRule featuresAreIndependent =
      slices()
          .matching("com.tglanz.atlas.backend.(*)..")
          .that(
              DescribedPredicate.describe(
                  "are features",
                  slice ->
                      !slice.getDescription().contains("shared")
                          && !slice.getDescription().contains("config")
                          && !slice.getDescription().contains("web")))
          .should()
          .notDependOnEachOther()
          .ignoreDependency(
              com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage(
                  "..adapter.out.movement.."),
              DescribedPredicate.alwaysTrue())
          .ignoreDependency(
              com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage(
                  "..adapter.out.avatar.."),
              DescribedPredicate.alwaysTrue());

  /**
   * Page composition may combine use-case ports, but never persistence or implementation classes.
   */
  @ArchTest
  static final ArchRule pagesUseApplicationPorts =
      noClasses()
          .that()
          .resideInAPackage("..backend.web..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..adapter..",
              "..application.service..",
              "..application.port.out..",
              "jakarta.persistence..",
              "org.springframework.data..");
}

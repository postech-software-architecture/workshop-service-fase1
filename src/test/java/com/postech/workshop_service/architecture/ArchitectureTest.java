package com.postech.workshop_service.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Testes de arquitetura que travam a regra de dependencia da Clean Architecture (setas
 * apontando para dentro): {@code api -> application -> domain <- infrastructure}.
 *
 * <p>
 * As classes de teste sao ignoradas ({@link ImportOption.DoNotIncludeTests}) porque a
 * regra vale para o codigo de producao; os testes legitimamente montam objetos de outras
 * camadas.
 * </p>
 */
@AnalyzeClasses(packages = "com.postech.workshop_service", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule domain_nao_depende_de_outras_camadas = noClasses().that()
		.resideInAPackage("..domain..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..api..", "..application..", "..infrastructure..", "org.springframework..",
				"jakarta.persistence..");

	@ArchTest
	static final ArchRule application_nao_depende_de_infra_nem_api = noClasses().that()
		.resideInAPackage("..application..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..infrastructure..", "..api..");

}

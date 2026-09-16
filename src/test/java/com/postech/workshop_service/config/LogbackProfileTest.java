package com.postech.workshop_service.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Garante que todo perfil de execucao esteja coberto por um bloco
 * <code>&lt;springProfile&gt;</code> do <code>logback-spring.xml</code>.
 *
 * <p>
 * O arquivo cobria apenas <code>default | local | test</code> e <code>prod</code>, mas o
 * deployment do Kubernetes (<code>k8s/base/deployment.yaml</code>) e o
 * <code>compose.yaml</code> ativam <code>docker</code>. Como nenhum bloco casava, o root
 * logger ficava sem appender e a aplicacao nao emitia log algum em producao — sem erro no
 * startup e sem pipeline vermelha. Foram 18 horas de pod com 12 linhas, todas do banner
 * do Spring Boot, impresso antes de o logback assumir.
 *
 * <p>
 * A falha e silenciosa por natureza: nada quebra, os logs simplesmente nao existem. So um
 * teste a pega, e por isso ele acompanha a correcao.
 */
class LogbackProfileTest {

	@ParameterizedTest(name = "perfil \"{0}\" coberto por algum springProfile")
	@ValueSource(strings = { "docker", "prod", "local", "test", "default" })
	@DisplayName("todo perfil de execucao tem bloco de logging")
	void perfilTemBlocoDeLogging(String perfil) throws Exception {
		Set<String> cobertos = perfisDeclarados();

		assertThat(cobertos)
			.as("perfil \"%s\" nao aparece em nenhum <springProfile> do logback-spring.xml; "
					+ "sem bloco correspondente o root logger fica sem appender e a aplicacao nao loga", perfil)
			.contains(perfil);
	}

	/** Coleta os nomes de perfil de todos os {@code <springProfile name="a | b">}. */
	private Set<String> perfisDeclarados() throws Exception {
		Set<String> nomes = new LinkedHashSet<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

		try (InputStream xml = getClass().getResourceAsStream("/logback-spring.xml")) {
			assertThat(xml).as("logback-spring.xml ausente no classpath").isNotNull();
			Document doc = factory.newDocumentBuilder().parse(xml);

			NodeList blocos = doc.getElementsByTagName("springProfile");
			assertThat(blocos.getLength()).as("nenhum <springProfile> declarado").isPositive();

			for (int i = 0; i < blocos.getLength(); i++) {
				String atributo = blocos.item(i).getAttributes().getNamedItem("name").getNodeValue();
				Arrays.stream(atributo.split("\\|")).map(String::trim).filter(s -> !s.isEmpty()).forEach(nomes::add);
			}
		}
		return nomes;
	}

}

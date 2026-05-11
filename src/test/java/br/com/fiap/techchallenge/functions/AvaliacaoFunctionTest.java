package br.com.fiap.techchallenge.functions;

import br.com.fiap.techchallenge.model.Avaliacao;
import br.com.fiap.techchallenge.repository.StorageTableRepository;
import br.com.fiap.techchallenge.service.EmailService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoFunctionTest {

    @Mock
    private StorageTableRepository repository;
    @Mock
    private EmailService emailService;
    @Mock
    private Validator validator;
    @Mock
    private HttpRequestMessage<Optional<String>> request;
    @Mock
    private ExecutionContext context;

    private AvaliacaoFunction avaliacaoFunction;

    @BeforeEach
    void setUp() {
        avaliacaoFunction = new AvaliacaoFunction(repository, emailService, validator);
        when(request.createResponseBuilder(any(HttpStatus.class))).thenAnswer(invocation -> {
            HttpStatus status = invocation.getArgument(0);
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        });
    }

    @Test
    void testRunComAvaliacaoValida() throws Exception {
        String json = "{\"descricao\":\"ótimo\",\"nota\":9}";
        when(request.getBody()).thenReturn(Optional.of(json));
        when(validator.validate(any(Avaliacao.class))).thenReturn(Collections.emptySet());

        HttpResponseMessage response = avaliacaoFunction.run(request, context);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        verify(repository, times(1)).salvarAvaliacao(any(Avaliacao.class));
        verify(emailService, never()).enviarNotificacaoCritica(any(Avaliacao.class));
    }

    @Test
    void testRunComAvaliacaoCritica() throws Exception {
        String json = "{\"descricao\":\"péssimo\",\"nota\":1}";
        when(request.getBody()).thenReturn(Optional.of(json));
        when(validator.validate(any(Avaliacao.class))).thenReturn(Collections.emptySet());

        HttpResponseMessage response = avaliacaoFunction.run(request, context);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        verify(repository, times(1)).salvarAvaliacao(any(Avaliacao.class));
        verify(emailService, times(1)).enviarNotificacaoCritica(any(Avaliacao.class));
    }

    @Test
    void testRunComErroValidacao() throws Exception {
        String json = "{\"nota\":9}";
        when(request.getBody()).thenReturn(Optional.of(json));
        ConstraintViolation<Avaliacao> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Descrição é obrigatória");
        when(validator.validate(any(Avaliacao.class))).thenReturn(Set.of(violation));

        HttpResponseMessage response = avaliacaoFunction.run(request, context);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        verify(repository, never()).salvarAvaliacao(any(Avaliacao.class));
    }
}

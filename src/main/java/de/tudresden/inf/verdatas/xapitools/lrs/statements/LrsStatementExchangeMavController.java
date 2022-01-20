package de.tudresden.inf.verdatas.xapitools.lrs.statements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsService;
import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIFlow;
import de.tudresden.inf.verdatas.xapitools.ui.IUIStep;
import de.tudresden.inf.verdatas.xapitools.ui.UIIcon;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@Order(2)
@Validated
public class LrsStatementExchangeMavController implements IUIFlow {

    private static final String BASE_URL = "/ui/statements";
    private final LrsService lrsService;

    public LrsStatementExchangeMavController(LrsService lrsService) {
        this.lrsService = lrsService;
    }

    @Override
    public String getName() {
        return "Statement Exchange";
    }

    @Override
    public String getEntrypoint() {
        return BASE_URL + "/";
    }

    @Override
    public List<IUIStep> getSteps() {
        return List.of();
    }

    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.ARROW_LR;
    }

    @GetMapping(BASE_URL + "/")
    public ModelAndView home() {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/statements");
        mav.addObject("lrsConnections", this.lrsService.getConnections(true));
        return mav;
    }

    @PostMapping(BASE_URL + "/insert")
    public RedirectView insertStatements(@RequestParam UUID targetLrs, @RequestParam List<MultipartFile> statementFile, RedirectAttributes attributes) {
        @Active LrsConnection lrsConnection = this.lrsService.getConnection(targetLrs);
        JsonMapper mapper = new JsonMapper();

        List<JsonNode> inputData = statementFile.stream()
                .map(multipartFile -> {
                    try {
                        return multipartFile.getInputStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .map((input) -> {
                    try {
                        return mapper.<List<JsonNode>>readValue(input, mapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class));
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Could not read file. Expected List of statements.");
                    }
                })
                .flatMap(List::stream)
                .toList();
        List<UUID> sendResult = this.lrsService.sendStatements(inputData, lrsConnection);
        attributes.addAttribute("success", sendResult.stream().filter(Objects::nonNull).count());
        return new RedirectView("./");
    }
}

package de.tudresden.inf.verdatas.xapitools.lrs.statements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsExceptions;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsService;
import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIFlow;
import de.tudresden.inf.verdatas.xapitools.ui.IUIStep;
import de.tudresden.inf.verdatas.xapitools.ui.UIIcon;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ModelAndView Controller for the xAPI Statement Exchange functionality.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(2)
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LrsStatementExchangeMavController implements IUIFlow {

    private static final String BASE_URL = "/ui/statements";
    private final LrsService lrsService;

    /**
     * Get the Human readable name of this sub-application.
     *
     * @return Name of sub-application
     */
    @Override
    public String getName() {
        return "Statement Exchange";
    }

    /**
     * Get the URL where the sub-application can be started.
     *
     * @return URL for Application Launch
     */
    @Override
    public String getEntrypoint() {
        return BASE_URL + "/";
    }

    /**
     * Get all Steps belonging to the sub-application, so they can be displayed alongside the Launcher.
     *
     * @return List of sub-app Steps
     */
    @Override
    public List<IUIStep> getSteps() {
        return List.of();
    }

    /**
     * Get the Icon for this UI Element
     *
     * @return Icon entity
     */
    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.ARROW_LR;
    }

    /**
     * Render the Homepage of this functionality.
     */
    @GetMapping(BASE_URL + "/")
    public ModelAndView home() {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/statements");
        // We can only send to Active Connections
        mav.addObject("lrsConnections", this.lrsService.getConnections(true));
        return mav;
    }

    /**
     * Send xAPI Statements from some input files to the given LRS
     *
     * @param targetLrs     ID of the destination LRS connection
     * @param statementFile List of files containing the xAPI Statements to push
     * @param attributes    -- Automatically bound by Spring.
     * @return 302 Redirect to {@link #home()} with exchanged statements count.
     */
    @PostMapping(BASE_URL + "/insert")
    public RedirectView insertStatements(@RequestParam UUID targetLrs, @RequestParam List<MultipartFile> statementFile, RedirectAttributes attributes) {
        @Active LrsConnection lrsConnection = this.lrsService.getConnection(targetLrs);
        // Read and parse first layer of files
        JsonMapper mapper = new JsonMapper();
        List<JsonNode> inputData = statementFile.stream()
                // Open file
                .map(multipartFile -> {
                    try {
                        return multipartFile.getInputStream();
                    } catch (IOException e) {
                        throw new LrsExceptions.BadInputData("Could not read file.");
                    }
                })
                // Read root node (list)
                .map((input) -> {
                    try {
                        return mapper.<List<JsonNode>>readValue(input, mapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class));
                    } catch (IOException e) {
                        throw new LrsExceptions.BadInputData("Could not read file. Expected List of statements.");
                    }
                })
                // Combine Lists to one
                .flatMap(List::stream)
                .toList();
        // Actual sending
        List<UUID> sendResult = this.lrsService.sendStatements(inputData, lrsConnection);
        attributes.addAttribute("success", sendResult.stream().filter(Objects::nonNull).count());
        return new RedirectView("./");
    }
}

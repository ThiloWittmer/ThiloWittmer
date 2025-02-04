package de.hsrm.mi.web.projekt.ui.benutzer;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.hsrm.mi.web.projekt.entities.benutzer.Benutzer;
import de.hsrm.mi.web.projekt.services.benutzer.BenutzerService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/benutzer")
@SessionAttributes({ "benutzerForm", "ueberschrift", "benNr", "maxWunsch", "benutzer" })
public class BenutzerController {

    @Autowired
    private BenutzerService benutzerService;

    @ModelAttribute("benutzerForm")
    public void creatForm(Model m) {
        m.addAttribute("benutzerForm", new BenutzerFormular());
    }

    @GetMapping
    public String getAllBenutzer(Model m) {
        List<Benutzer> benutzerListe = benutzerService.holeAlleBenutzer();
        m.addAttribute("benutzerListe", benutzerListe);
        return "benutzer/benutzerliste";
    }

    @GetMapping("/{id}/del")
    public String deleteBenutzer(@PathVariable("id") Long id) {
        benutzerService.loescheBenutzerMitId(id);
        return "redirect:/benutzer";
    }

    @GetMapping("/{benutzerNr}")
    public String benutzerProfil(@PathVariable("benutzerNr") long benNr, Model m,
            @ModelAttribute("benutzerForm") BenutzerFormular benutzerForm, Locale locale) {
        int maxWunsch = 5;
        m.addAttribute("sprache", locale.getDisplayLanguage());
        m.addAttribute("langCode", locale.getLanguage());
        m.addAttribute("maxWunsch", "(max. " + maxWunsch + ")");
        m.addAttribute("benNr", (benNr));

        if (benNr == 0) {
            Benutzer benutzer = new Benutzer();
            BenutzerFormular bForm = new BenutzerFormular();

            m.addAttribute("benutzer", benutzer);
            m.addAttribute("benutzerForm", bForm);
        } else {
            Benutzer benutzer;
            benutzer = benutzerService.holeBenutzerMitId(benNr).get();
            benutzerForm.fromBenutzer(benutzer);

            m.addAttribute("benutzer", benutzer);
            m.addAttribute("benutzerForm", benutzerForm);
        }
        return "benutzer/benutzerbearbeiten";
    }

    @PostMapping("{benNr}")
    public String postForm(@Valid @ModelAttribute("benutzerForm") BenutzerFormular benutzerForm, BindingResult formErrors,
            @ModelAttribute("benNr") long benNr, Model m, @ModelAttribute("benutzer") Benutzer benutzer) {

        String pw = benutzerForm.getPasswort();

        if (formErrors.hasErrors()) {
            return "benutzer/benutzerbearbeiten";
        }

        // Benutzer existiert noch nicht und kein gueltiges Passwort
        if (pw.length() == 0 && benutzerService.holeBenutzerMitId(benNr).isEmpty()) {
            formErrors.rejectValue("passwort", "benutzer.passwort.ungesetzt", "Passwort wurde noch nicht gesetzt");
            return "benutzer/benutzerbearbeiten";
        }

        // Wenn der Benutzer nicht schon existiert und das Passwortfeld leer ist
        if (!(benutzerService.holeBenutzerMitId(benNr).isPresent() && pw.length() == 0)) {
            benutzer.setPassword(pw);
        }

        try {
            benutzerForm.toBenutzer(benutzer);
            benutzerService.speichereBenutzer(benutzer);
        } catch (Exception e) {
            String excMsg = e.getLocalizedMessage();
            m.addAttribute("info", excMsg);
            return "benutzer/benutzerbearbeiten";
        }

        if (benNr > 0) {
            return "benutzer/benutzerbearbeiten";
        }

        return "redirect:/benutzer/" + benutzer.getId();
    }

}

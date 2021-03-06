package pl.com.turski.controller.shipment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import pl.com.turski.model.domain.location.Location;
import pl.com.turski.model.domain.shipment.Shipment;
import pl.com.turski.model.domain.shipment.ShipmentMovement;
import pl.com.turski.model.domain.shipment.ShipmentStatusEnum;
import pl.com.turski.model.view.shipment.ShipmentCheck;
import pl.com.turski.model.view.shipment.ShipmentCreate;
import pl.com.turski.service.shipment.ShipmentMovementService;
import pl.com.turski.service.shipment.ShipmentService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Adam
 */
@Controller
@RequestMapping("/shipment")
public class ShipmentController {

    private static final Logger LOG = LoggerFactory.getLogger(ShipmentController.class);

    @Autowired
    private ShipmentService shipmentService;
    @Autowired
    private ShipmentMovementService shipmentMovementService;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String getCreate(Model model) {
        model.addAttribute("shipmentCreate", new ShipmentCreate());
        return "shipment/create";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@Valid @ModelAttribute("shipmentCreate") ShipmentCreate shipmentCreate, BindingResult result) {
        if (result.hasErrors()) {
            return "shipment/create";
        }

        shipmentService.create(shipmentCreate,2L);
        return "redirect:/shipment/list";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(ModelMap model, @RequestParam(value = "statusFilter", required = false) ShipmentStatusEnum statusFilter) {
        List<Shipment> shipments = shipmentService.getAll(statusFilter);
        model.put("shipments", shipments);
        model.put("statuses", ShipmentStatusEnum.values());
        model.put("statusFilter", statusFilter);
        return "shipment/list";
    }

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    public String getCheck(ModelMap model) {
        model.addAttribute("shipmentCheck", new ShipmentCheck());
        return "shipment/check";
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public String check(@Valid @ModelAttribute("shipmentCheck") ShipmentCheck shipmentCheck, BindingResult result) {
        if (result.hasErrors()) {
            return "shipment/check";
        }
        boolean shipmentExist = shipmentService.isExist(shipmentCheck.getId());
        if(!shipmentExist){
            result.rejectValue("id", "shipmentNotFound");
            return "shipment/check";
        }

        return "redirect:/shipment/history?id=" + shipmentCheck.getId();
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public String history(ModelMap model, @RequestParam(value = "id") Long id) {
        model.addAttribute("shipment", shipmentService.get(id));
        List<ShipmentMovement> movements = shipmentMovementService.getMovements(id);
        model.addAttribute("shipmentMovements", movements);
        List<Location> locations = new ArrayList<>();
        for (ShipmentMovement movement : movements) {
            locations.add(movement.getStation().getLocation());
        }
        model.addAttribute("locations", locations);

        return "shipment/history";
    }

}

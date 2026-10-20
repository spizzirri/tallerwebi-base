package com.valhalla.presentation.place;

import com.valhalla.domain.exception.PlaceInUseException;
import com.valhalla.domain.exception.PlaceNotFoundException;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ABM de places bajo /admin/places, espejo de {@code UserController}: listado, alta, edicion y
 * baja protegida. La baja chequea el uso en planes antes de tocar la fila (AC-11) y, si el lugar
 * esta en un plan, vuelve al listado con el motivo en flash attribute sin borrar nada.
 */
@Controller
@RequestMapping("/admin/places")
public class PlaceAdminController {

  private static final String VIEW_PLACES = "pages/admin/places";
  private static final String VIEW_PLACE_FORM = "pages/admin/place-form";
  private static final String REDIRECT_PLACES = "redirect:/admin/places";
  private static final String ATTR_PLACES = "places";
  private static final String ATTR_PLACE_FORM = "placeForm";
  private static final String ATTR_PLACE_ID = "placeId";
  private static final String ATTR_IS_EDIT = "isEdit";
  private static final String ATTR_CATEGORIES = "categories";
  private static final String ATTR_ERROR = "error";
  private static final String ERROR_INVALID_DATA = "Invalid place data";
  private static final String ERROR_PLACE_IN_USE =
    "The place is used by a plan and cannot be deleted";

  private final PlaceService placeService;

  @Autowired
  public PlaceAdminController(PlaceService placeService) {
    this.placeService = placeService;
  }

  @GetMapping
  public ModelAndView listPlaces() {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLACES, placeService.getAllPlaces());
    model.put(ATTR_CATEGORIES, PlaceCategory.values());
    return new ModelAndView(VIEW_PLACES, model);
  }

  @GetMapping("/new")
  public ModelAndView showNewPlaceForm(@RequestParam Map<String, String> params) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLACE_FORM, formFromQuery(params));
    model.put(ATTR_IS_EDIT, false);
    model.put(ATTR_CATEGORIES, PlaceCategory.values());
    return new ModelAndView(VIEW_PLACE_FORM, model);
  }

  @PostMapping
  public ModelAndView createPlace(
    @Valid @ModelAttribute(ATTR_PLACE_FORM) EditPlaceRequest placeForm,
    BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      return renderFormWithError(placeForm, false);
    }
    placeService.create(placeForm);
    return new ModelAndView(REDIRECT_PLACES);
  }

  @GetMapping("/{id}/edit")
  public ModelAndView showEditPlaceForm(@PathVariable Long id) {
    Place place = placeService.getPlaceById(id).orElseThrow(PlaceNotFoundException::new);
    EditPlaceRequest form = new EditPlaceRequest();
    form.setName(place.getName());
    form.setCategory(place.getCategory());
    form.setDescription(place.getDescription());
    form.setAddress(place.getAddress());
    form.setOpeningHours(place.getOpeningHours());
    form.setPhone(place.getPhone());
    form.setWebsite(place.getWebsite());
    form.setImageUrl(place.getImageUrl());
    form.setLatitude(place.getLatitude());
    form.setLongitude(place.getLongitude());
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLACE_FORM, form);
    model.put(ATTR_PLACE_ID, id);
    model.put(ATTR_IS_EDIT, true);
    model.put(ATTR_CATEGORIES, PlaceCategory.values());
    return new ModelAndView(VIEW_PLACE_FORM, model);
  }

  @RequestMapping(value = "/{id}", method = { RequestMethod.POST, RequestMethod.PUT })
  public ModelAndView updatePlace(
    @PathVariable Long id,
    @Valid @ModelAttribute(ATTR_PLACE_FORM) EditPlaceRequest placeForm,
    BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      return renderFormWithError(placeForm, true, id);
    }
    placeService.update(id, placeForm);
    return new ModelAndView(REDIRECT_PLACES);
  }

  @RequestMapping(value = "/{id}/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
  public ModelAndView deletePlace(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      placeService.delete(id);
    } catch (PlaceInUseException exception) {
      // AC-11: no se borra nada y el motivo viaja al listado por flash attribute.
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ERROR_PLACE_IN_USE);
    }
    return new ModelAndView(REDIRECT_PLACES);
  }

  /**
   * I-17: el importador pre-llena el alta por query params. Sin params el form arranca vacio,
   * igual que antes del importador (I-06..I-12).
   */
  private EditPlaceRequest formFromQuery(Map<String, String> params) {
    EditPlaceRequest form = new EditPlaceRequest();
    if (params.isEmpty()) {
      return form;
    }
    form.setName(params.get("name"));
    form.setAddress(params.get("address"));
    form.setOpeningHours(params.get("openingHours"));
    form.setPhone(params.get("phone"));
    form.setWebsite(params.get("website"));
    form.setLatitude(numberOrNull(params.get("latitude")));
    form.setLongitude(numberOrNull(params.get("longitude")));
    // AC-06: una categoria sin mapping (o desconocida) arranca en OTHER; el admin la cambia.
    form.setCategory(PlaceCategory.fromCode(params.get("category")).orElse(PlaceCategory.OTHER));
    return form;
  }

  private Double numberOrNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Double.valueOf(value);
    } catch (NumberFormatException exception) {
      // Un query param manipulado no puede romper el form: se ignora el valor.
      return null;
    }
  }

  private ModelAndView renderFormWithError(EditPlaceRequest form, boolean isEdit) {
    return renderFormWithError(form, isEdit, null);
  }

  private ModelAndView renderFormWithError(EditPlaceRequest form, boolean isEdit, Long id) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLACE_FORM, form);
    model.put(ATTR_IS_EDIT, isEdit);
    model.put(ATTR_CATEGORIES, PlaceCategory.values());
    if (id != null) {
      model.put(ATTR_PLACE_ID, id);
    }
    model.put(ATTR_ERROR, ERROR_INVALID_DATA);
    return new ModelAndView(VIEW_PLACE_FORM, model);
  }
}

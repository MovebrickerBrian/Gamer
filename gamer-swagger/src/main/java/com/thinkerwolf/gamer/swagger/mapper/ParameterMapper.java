package com.thinkerwolf.gamer.swagger.mapper;

import com.thinkerwolf.gamer.swagger.schema.AllowableValues;
import com.thinkerwolf.gamer.swagger.schema.ModelReference;
import com.thinkerwolf.gamer.swagger.schema.Types;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;

import static com.thinkerwolf.gamer.swagger.mapper.Properties.*;

public class ParameterMapper {

    public Parameter mapParameter(com.thinkerwolf.gamer.swagger.dto.Parameter source) {
        Parameter p = bodyParameter(source);
        return p;
    }

    private Parameter bodyParameter(com.thinkerwolf.gamer.swagger.dto.Parameter source) {
        Model model = fromModelRef(source.getModelRef());
        BodyParameter parameter = new BodyParameter()
                .description(source.getDescription())
                .name(source.getName())
                .schema(model);
        parameter.setAccess(source.getAccess());
        parameter.setRequired(source.isRequired());

        Property property = property(source.getModelRef());
        QueryParameter query = new QueryParameter().description(source.getDescription())
                .name(source.getName()).type(source.getModelRef().getType()).property(property);

        return query;
    }

    Model fromModelRef(ModelReference modelRef) {
        if (modelRef.isCollection()) {
            if (modelRef.getItemType().equals("byte")) {
                ModelImpl baseModel = new ModelImpl();
                baseModel.setType("string");
                baseModel.setFormat("byte");
                return maybeAddAllowableValuesToParameter(baseModel, modelRef.getAllowableValues());
            }
            ModelReference itemModel = modelRef.itemModel().get();
            return new ArrayModel()
                    .items(maybeAddAllowableValues(itemTypeProperty(itemModel), itemModel.getAllowableValues()));
        }
        if (modelRef.isMap()) {
            ModelImpl baseModel = new ModelImpl();
            ModelReference itemModel = modelRef.itemModel().get();
            baseModel.additionalProperties(maybeAddAllowableValues(itemTypeProperty(itemModel), itemModel.getAllowableValues()));
            return baseModel;
        }
        if (Types.isBaseType(modelRef.getType())) {
            Property property = property(modelRef.getType());
            ModelImpl baseModel = new ModelImpl();
            baseModel.setType(property.getType());
            baseModel.setFormat(property.getFormat());
            return maybeAddAllowableValuesToParameter(baseModel, modelRef.getAllowableValues());

        }
        return new RefModel(modelRef.getType());
    }



    static ModelImpl maybeAddAllowableValuesToParameter(
            ModelImpl toReturn,
            AllowableValues allowableValues) {

//        if (allowableValues instanceof AllowableListValues) {
//            toReturn.setEnum(((AllowableListValues) allowableValues).getValues());
//        }
//        if (allowableValues instanceof AllowableRangeValues) {
//            AllowableRangeValues range = (AllowableRangeValues) allowableValues;
//            toReturn.setMinimum(safeDouble(range.getMin()));
//            toReturn.setMaximum(safeDouble(range.getMax()));
//        }
        return toReturn;
    }
}

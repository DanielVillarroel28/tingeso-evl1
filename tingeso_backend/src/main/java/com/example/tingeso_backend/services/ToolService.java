package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ToolService {
    @Autowired
    ToolRepository toolRepository;

    @Autowired
    private KardexService kardexService;

    public ArrayList<ToolEntity> getTools(){
        return (ArrayList<ToolEntity>) toolRepository.findAll();
    }

    public ToolEntity saveTool(ToolEntity tool){
        String currentUser = "ADMIN";
        tool.setStatus("Disponible");
        ToolEntity newTool = toolRepository.save(tool);
        kardexService.createNewToolMovement(newTool, currentUser);
        return newTool;
    }

    public ToolEntity getToolById(Long id){
        return toolRepository.findById(id).get();
    }

    public ToolEntity updateTool(Long id, ToolEntity tool) {
        return toolRepository.save(tool);
    }

    public boolean deleteTool(Long id) throws Exception {
        try{
            toolRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }
    public boolean logicalDeleteTool(Long id) {
        // Buscar la herramienta existente
        ToolEntity tool = toolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Herramienta no encontrada con id: " + id));

        // Cambiar el estado en lugar de borrar
        tool.setStatus("Dada de baja");
        toolRepository.save(tool);

        String currentUser = "ADMIN"; // Obtener del contexto de seguridad
        kardexService.createWriteOffMovement(tool, currentUser);


        return true;
    }

}

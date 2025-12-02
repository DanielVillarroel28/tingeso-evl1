package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.repositories.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Map;

@Service
public class ClientService {
    @Autowired
    ClientRepository clientRepository;

    public ArrayList<ClientEntity> getClients(){
        return (ArrayList<ClientEntity>) clientRepository.findAll();
    }

    public ClientEntity saveEmployee(ClientEntity employee){

        return clientRepository.save(employee);
    }

    public ClientEntity getClientById(Long id){
        return clientRepository.findById(id).get();
    }

    public ClientEntity getClientByRut(String rut){

        return clientRepository.findByRut(rut);
    }

    public ClientEntity updateClient(Long id, ClientEntity clientDetails) {
        //buscar el cliente existente por su id
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id));

        client.setName(clientDetails.getName());
        client.setRut(clientDetails.getRut());
        client.setPhone(clientDetails.getPhone());
        client.setEmail(clientDetails.getEmail());
        client.setStatus(clientDetails.getStatus());

        return clientRepository.save(client);
    }
    public boolean deleteClient(Long id) throws Exception {
        try{
            clientRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    @Transactional
    public ClientEntity findOrCreateClient(JwtAuthenticationToken principal) {
        //  id unico del usuario desde el token
        String keycloakId = principal.getName();

        // 2. Busca el cliente en tu base de datos. Si no existe, ejecuta el código para crearlo.
        return clientRepository.findByKeycloakId(keycloakId).orElseGet(() -> {
            System.out.println("Cliente con Keycloak ID '" + keycloakId + "' no encontrado. Creando nuevo cliente...");

            // 3. Extrae todos los "claims" (datos) del token.
            Map<String, Object> claims = principal.getToken().getClaims();

            // 4. Obtiene los datos estándar y personalizados.
            String name = (String) claims.get("name");
            String email = (String) claims.get("email");
            String rut = (String) claims.get("RUT");       // Atributo personalizado RUT
            String phone = (String) claims.get("phone");     // Atributo personalizado Teléfono (en minúscula)

            // 5. Crea y configura la nueva entidad de cliente.
            ClientEntity newClient = new ClientEntity();
            newClient.setKeycloakId(keycloakId);
            newClient.setName(name);
            newClient.setEmail(email);
            newClient.setRut(rut);       // Asigna el RUT
            newClient.setPhone(phone);   // Asigna el teléfono
            newClient.setStatus("Activo"); // Define un estado inicial

            System.out.println("Nuevo cliente creado -> Nombre: " + name + ", RUT: " + rut);

            // 6. Guarda el nuevo cliente en la base de datos y lo retorna.
            return clientRepository.save(newClient);
        });
    }

    public ClientEntity getCurrentClient(JwtAuthenticationToken principal) {
        String keycloakId = principal.getName(); // 'sub' del token
        return clientRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró un perfil de cliente para el usuario actual."));
    }

    /**
     * Actualiza la información del cliente actualmente autenticado.
     */
    public ClientEntity updateCurrentClient(JwtAuthenticationToken principal, ClientEntity clientDetails) {
        ClientEntity clientToUpdate = getCurrentClient(principal);

        // Actualiza solo los campos permitidos
        clientToUpdate.setName(clientDetails.getName());
        clientToUpdate.setRut(clientDetails.getRut());
        clientToUpdate.setPhone(clientDetails.getPhone());
        clientToUpdate.setEmail(clientDetails.getEmail());

        return clientRepository.save(clientToUpdate);
    }
}

package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelos.TelefoneAtualizador;
import com.autobots.automanager.modelos.TelefoneSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@RestController
@RequestMapping("/telefones")
public class TelefoneControle {
	
	@Autowired
	private ClienteRepositorio repositorio;
	
	@Autowired
	private TelefoneRepositorio repositorioTelefone;
	
	@Autowired
	private TelefoneSelecionador selecionadorTelefone;
	
	// get all
	@GetMapping
	public ResponseEntity<List<Telefone>> obterTelefones() {
		List<Telefone> telefones = repositorioTelefone.findAll();
		return telefones.isEmpty() ?
                new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(telefones, HttpStatus.OK);	
	}
	
	// get by id
	@GetMapping("/{telId}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long telId) {
		Telefone telefone = selecionadorTelefone.selecionar(repositorioTelefone.findAll(), telId);
		return telefone == null ?
				new ResponseEntity<>(HttpStatus.NOT_FOUND) :
				new ResponseEntity<>(telefone, HttpStatus.OK);
	}
	
	// cadastrar telefone no cliente
    @PostMapping("/cadastro/{clienteId}")
    public ResponseEntity<Void> cadastrarTelefone(@PathVariable long clienteId, @RequestBody Telefone telefone) {
        Cliente cliente = repositorio.findById(clienteId).orElse(null);
        if (cliente != null) {
            telefone = repositorioTelefone.save(telefone); 
            cliente.getTelefones().add(telefone); 
            repositorio.save(cliente); 
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
	
	// atualizar telefone
	@PutMapping("/atualizar/{telId}")
	public ResponseEntity<Void> atualizarTelefone(@PathVariable long telId, @RequestBody Telefone atualizacao) {
		Telefone telefone = repositorioTelefone.findById(telId).orElse(null);
		if (telefone != null) {
			new TelefoneAtualizador().atualizar(telefone, atualizacao);
			repositorioTelefone.save(telefone);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// deletar telefone
	@DeleteMapping("/excluir/{clienteId}/{telId}")
	public ResponseEntity<Void> deletarTelefone(@PathVariable long clienteId, @PathVariable long telId) {
		Cliente cliente = repositorio.findById(clienteId).orElse(null);
		Telefone telefone = repositorioTelefone.findById(telId).orElse(null);
		if (cliente != null && telefone != null) {
			cliente.getTelefones().remove(telefone);
			repositorioTelefone.delete(telefone);
			repositorio.save(cliente);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
}

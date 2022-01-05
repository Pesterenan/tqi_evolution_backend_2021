package com.pesterenan.tqi.services;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.pesterenan.tqi.domain.Cidade;
import com.pesterenan.tqi.domain.Cliente;
import com.pesterenan.tqi.domain.Endereco;
import com.pesterenan.tqi.dto.ClienteDTO;
import com.pesterenan.tqi.dto.ClienteNovoDTO;
import com.pesterenan.tqi.repositories.ClienteRepository;
import com.pesterenan.tqi.services.exceptions.DataIntegrityException;
import com.pesterenan.tqi.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	public Cliente find(Long id) throws ObjectNotFoundException {
		Optional<Cliente> obj = clienteRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto do tipo: " + Cliente.class.getName() + " não encontrado! Id: " + id));
	}

	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = clienteRepository.save(obj);
		return obj;
	}

	// Cria um novo objetoCliente e atualiza somente os dados necessários pra depois
	// salvar no banco de dados.
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return clienteRepository.save(newObj);
	}

	// Atualiza dados de nome e email do novo objeto cliente para poder manter
	// outros dados imutáveis.
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}

	public void deleteById(Long id) {
		find(id);
		try {
			clienteRepository.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir um porque há empréstimos relacionados.");
		}
	}

	public List<Cliente> findAll() {
		List<Cliente> list = clienteRepository.findAll();
		return list;
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String direction, String orderBy) {
		PageRequest pageReq = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return clienteRepository.findAll(pageReq);
	}

	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), null, null, 0, objDto.getEmail(), null);
	}

	public Cliente fromDTO(ClienteNovoDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getCpf(), objDto.getRg(), objDto.getRenda(), objDto.getEmail(), objDto.getSenha());
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(),
				objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2() != null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3() != null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}
		return cli;
	}

}
